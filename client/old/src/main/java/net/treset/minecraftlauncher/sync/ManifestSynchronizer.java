package net.treset.minecraftlauncher.sync;

import javafx.util.Pair;
import net.treset.mc_version_loader.json.SerializationException;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.util.DownloadStatus;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.util.file.LauncherFile;
import net.treset.minecraftlauncher.util.string.FormatString;
import net.treset.minecraftlauncher.util.string.UrlString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ManifestSynchronizer extends FileSynchronizer {
    private LauncherManifest manifest;
    protected LauncherFiles files;

    public ManifestSynchronizer(LauncherManifest manifest, LauncherFiles files, SyncCallback callback) {
        super(callback);
        this.manifest = manifest;
        this.files = files;
    }

    @Override
    public void upload() throws IOException {
        if(SyncService.isSyncing(manifest)) {
            uploadExisting();
        } else {
            uploadNew();
        }

    }

    @Override
    public void download() throws IOException {
        if(SyncService.isSyncing(manifest)) {
            downloadExisting();
        } else {
            downloadNew();
        }
    }

    protected void uploadExisting() throws IOException {
        setStatus(new SyncStatus(SyncStep.COLLECTING, null));
        ComponentData currentData = getCurrentComponentData();
        ComponentData newData = calculateComponentData(currentData.getVersion());
        List<String> difference = compareHashes(new ComponentData.HashEntry("", currentData.getHashTree()), new ComponentData.HashEntry("", newData.getHashTree()), LauncherFile.of("./"));
        uploadDifference(difference);
        completeUpload(newData);
        setStatus(new SyncStatus(SyncStep.FINISHED, null));
    }

    protected void uploadNew() throws IOException {
        setStatus(new SyncStatus(SyncStep.STARTING, null));
        LOGGER.debug("Sync file not found, creating new sync file");
        setStatus(new SyncStatus(SyncStep.COLLECTING, null));
        ComponentData data = calculateComponentData(0);
        setStatus(new SyncStatus(SyncStep.UPLOADING, null));
        SyncService service = new SyncService();
        service.newComponent(SyncService.convertType(manifest.getType()), manifest.getId());
        uploadAll(data);
        LOGGER.debug("Completing upload...");
        int version = completeUpload(data);
        LOGGER.debug("Upload complete: version=" + version);
        setStatus(new SyncStatus(SyncStep.FINISHED, null));
    }

    protected void downloadExisting() throws IOException {
        ComponentData data = getCurrentComponentData();

        downloadDifference(data.getVersion());
    }

    protected void downloadNew() throws IOException {
        File dir = new File(manifest.getDirectory());
        if(!dir.exists()) {
            Files.createDirectories(dir.toPath());
        }

        downloadDifference(0);

        LauncherManifest parent = getParentManifest();
        LOGGER.debug("Adding component to parent manifest");
        parent.getComponents().add(manifest.getId());
        String fileName = LauncherApplication.config.MANIFEST_FILE_NAME;
        if(manifest.getType() == LauncherManifestType.MODS_COMPONENT) {
            fileName = files.getGameDetailsManifest().getComponents().get(0);
        } else if(manifest.getType() == LauncherManifestType.SAVES_COMPONENT) {
            fileName = files.getGameDetailsManifest().getComponents().get(1);
        }
        LauncherFile.of(parent.getDirectory(), fileName).write(parent);
    }

    protected LauncherManifest getParentManifest() throws IOException {
        switch(manifest.getType()) {
            case SAVES_COMPONENT -> {
                return files.getSavesManifest();
            }
            case MODS_COMPONENT -> {
                return files.getModsManifest();
            }
            case RESOURCEPACKS_COMPONENT -> {
                return files.getResourcepackManifest();
            }
            case OPTIONS_COMPONENT -> {
                return files.getOptionsManifest();
            }
            case INSTANCE_COMPONENT ->  {
                return files.getInstanceManifest();
            }
            default -> throw new IOException("Invalid component type");
        }
    }

    protected void uploadAll(ComponentData data) throws IOException {
        SyncService service = new SyncService();
        uploadDirectory(data.getHashTree(), SyncService.convertType(manifest.getType()), manifest.getId(), manifest.getDirectory(), "", 0, data.getFileAmount(), service);
    }

    protected Integer uploadDirectory(List<ComponentData.HashEntry> entries, String type, String id, String basePath, String filePath, Integer currentAmount, Integer totalAmount, SyncService service) throws IOException {
        for(ComponentData.HashEntry child : entries) {
            if(child.getChildren() == null) {
                LOGGER.debug("Uploading file: " + child.getPath());
                currentAmount++;
                setStatus(new SyncStatus(SyncStep.UPLOADING, new DownloadStatus(currentAmount, totalAmount, child.getPath(), false)));
                LauncherFile file = LauncherFile.of(basePath, filePath, child.getPath());
                byte[] content = file.read();
                service.uploadFile(type, id, file.getPath(), content);
            } else {
                currentAmount = uploadDirectory(child.getChildren(), type, id, basePath, LauncherFile.of(filePath, child.getPath()).getPath(), currentAmount, totalAmount, service);
            }
        }
        return currentAmount;
    }

    protected void uploadDifference(List<String> difference) throws IOException {
        if(difference.isEmpty()) {
            LOGGER.debug("No difference found");
            return;
        }
        setStatus(new SyncStatus(SyncStep.UPLOADING, null));
        SyncService syncService = new SyncService();
        int index = 0;
        for(String path : difference) {
            index++;
            setStatus(new SyncStatus(SyncStep.UPLOADING, new DownloadStatus(index, difference.size(), path, false)));
            LOGGER.debug("Difference: " + path);
            LauncherFile file = LauncherFile.of(manifest.getDirectory(), path);
            byte[] content;
            if(file.isFile()) {
                content = file.read();
            } else {
                content = new byte[]{};
            }
            syncService.uploadFile(SyncService.convertType(manifest.getType()), manifest.getId(), path, content);
        }
    }

    protected int completeUpload(ComponentData newData) throws IOException {
        int version = new SyncService().complete(SyncService.convertType(manifest.getType()), manifest.getId());
        newData.setVersion(version);
        getSyncFile().write(newData);
        return version;
    }

    protected List<String> compareHashes(ComponentData.HashEntry oldEntry, ComponentData.HashEntry newEntry, LauncherFile path) {
        ArrayList<String> difference = new ArrayList<>();
        if(oldEntry.getChildren() == null && newEntry.getChildren() == null) {
            if(!oldEntry.getHash().equals(newEntry.getHash())) {
                LOGGER.debug("Adding changed file: " + path);
                difference.add(path.getPath());
            }
        } else if(oldEntry.getChildren() != null && newEntry.getChildren() != null) {
            int j = 0;
            for(int i = 0; i < oldEntry.getChildren().size(); i++) {
                boolean found = false;
                for(int k = j; k < newEntry.getChildren().size(); k++) {
                    if(oldEntry.getChildren().get(i).getPath().equals(newEntry.getChildren().get(k).getPath())) {
                        found = true;
                        for(int l = j; l < k; l++) {
                            LOGGER.debug("Adding added file: " + path + "/" + newEntry.getChildren().get(l).getPath());
                            difference.addAll(getAllChildren(newEntry.getChildren().get(l), LauncherFile.of(path, newEntry.getChildren().get(l).getPath())));
                        }
                        difference.addAll(compareHashes(oldEntry.getChildren().get(i), newEntry.getChildren().get(k), LauncherFile.of(path, oldEntry.getChildren().get(i).getPath())));
                        j = k+1;
                        break;
                    }
                }
                if(!found) {
                    LOGGER.debug("Adding deleted file: " + oldEntry.getChildren().get(i).getPath());
                    difference.addAll(getAllChildren(oldEntry.getChildren().get(i), LauncherFile.of(path,  oldEntry.getChildren().get(i).getPath())));
                }
            }
        } else if(oldEntry.getChildren() == null) {
            LOGGER.debug("Adding file that became folder: " + newEntry.getPath());
            difference.add(oldEntry.getPath());
            difference.addAll(getAllChildren(newEntry, path));
        } else {
            LOGGER.debug("Adding folder that became file: " + newEntry.getPath());
            difference.addAll(getAllChildren(oldEntry, path));
            difference.add(newEntry.getPath());
        }
        return difference;
    }

    protected List<String> getAllChildren(ComponentData.HashEntry entry, LauncherFile path) {
        ArrayList<String> children = new ArrayList<>();
        if(entry.getChildren() == null) {
            children.add(path.getPath());
        } else {
            for(ComponentData.HashEntry child : entry.getChildren()) {
                children.addAll(getAllChildren(child, LauncherFile.of(path, child.getPath())));
            }
        }
        return children;
    }

    protected ComponentData getCurrentComponentData() throws IOException {
        LauncherFile syncFile = LauncherFile.of(manifest.getDirectory(), LauncherApplication.config.SYNC_FILENAME);
        if(!syncFile.exists()) {
            throw new IOException("Sync file not found");
        }
        String componentData = syncFile.readString();
        try {
            return ComponentData.fromJson(componentData);
        } catch (SerializationException e) {
            throw new IOException("Failed to parse sync file", e);
        }
    }

    protected void downloadDifference(int currentVersion) throws IOException {
        setStatus(new SyncStatus(SyncStep.COLLECTING, null));
        GetResponse response = new SyncService().get(SyncService.convertType(manifest.getType()), manifest.getId(), currentVersion);
        if(response.getVersion() == currentVersion) {
            LOGGER.debug("Component is up to date");
            setStatus(new SyncStatus(SyncStep.FINISHED, null));
            return;
        }

        LOGGER.debug("Downloading component: version=" + currentVersion + " -> " + response.getVersion());
        setStatus(new SyncStatus(SyncStep.DOWNLOADING, null));

        downloadFiles(response.getDifference());

        LOGGER.debug("Updating sync file");
        setStatus(new SyncStatus(SyncStep.COLLECTING, null));
        updateSyncFile(response.getVersion());
        LOGGER.debug("Update complete");
        setStatus(new SyncStatus(SyncStep.FINISHED, null));
    }

    protected void downloadFiles(List<String> difference) throws IOException {
        SyncService service = new SyncService();
        String type = SyncService.convertType(manifest.getType());
        String basePath = manifest.getDirectory();
        int amount = 0;
        for(String path : difference) {
            try {
                path = UrlString.decoded(path).get();
            } catch (FormatString.FormatException e) {
                LOGGER.warn("Unable to decode filepath: " + path + ", this may be due to no url encoding being used, continuing with possibly encoded path, error: " + e);
            }
            LOGGER.debug("Downloading file: " + path);
            setStatus(new SyncStatus(SyncStep.DOWNLOADING, new DownloadStatus(++amount, difference.size(), path, false)));
            byte[] content = service.downloadFile(type, manifest.getId(), path);
            LauncherFile file = LauncherFile.of(basePath, path);
            if(content.length == 0) {
                if(file.isFile()) {
                    LOGGER.debug("Deleting file or dir: " + path);
                    file.remove();
                }
            } else {
                if(!file.isFile())  {
                    if(file.isDirectory()) {
                        file.remove();
                    }
                    file.createFile();
                }
                file.write(content);
            }
        }
    }

    protected ComponentData updateSyncFile(int version) throws IOException {
        ComponentData data = calculateComponentData(version);
        getSyncFile().write(data);
        return data;
    }

    protected LauncherFile getSyncFile() {
        return LauncherFile.of(manifest.getDirectory(), LauncherApplication.config.SYNC_FILENAME);
    }

    protected ComponentData calculateComponentData(int version) throws IOException {
        LOGGER.debug("Collecting component data for component: " + manifest.getId());
        long startTime = System.currentTimeMillis();
        File componentDir = new File(manifest.getDirectory());
        resetCount();
        List<ComponentData.HashEntry> result = hashDirectoryContents(componentDir);
        LOGGER.debug("Component data collected in " + (System.currentTimeMillis() - startTime) + "ms");
        return new ComponentData(version, resetCount(), result);
    }

    protected List<ComponentData.HashEntry> hashDirectoryContents(File dir) throws IOException {
        File[] children = dir.listFiles((dir1, name) -> !name.equals("data.sync") && !name.equals(".included_files_old"));
        if(children == null) return List.of();
        ArrayList<Pair<File, Integer>> files = new ArrayList<>();
        ArrayList<ComponentData.HashEntry> hashTree = new ArrayList<>();
        for(int i = 0; i < children.length; i++) {
            files.add(new Pair<>(children[i], i));
            hashTree.add(null);
        }
        ArrayList<IOException> exceptions = new ArrayList<>();
        files.parallelStream().forEach((file) -> {
            if(file.getKey().isDirectory()) {
                List<ComponentData.HashEntry> result;
                try {
                    result = hashDirectoryContents(file.getKey());
                } catch (IOException e) {
                    exceptions.add(e);
                    return;
                }
                hashTree.set(file.getValue(), new ComponentData.HashEntry(file.getKey().getName(), result));
            } else if(file.getKey().isFile()) {
                addCount(1);
                try {
                    hashTree.set(file.getValue(), new ComponentData.HashEntry(file.getKey().getName(), LauncherFile.of(file.getKey()).hash()));
                } catch (IOException e) {
                    exceptions.add(e);
                }
            }
        });
        if(!exceptions.isEmpty()) {
            throw exceptions.get(0);
        }
        return hashTree;
    }

    private int count = 0;
    protected void addCount(int amount) {
        this.count += amount;
    }
    protected int resetCount() {
        int count = this.count;
        this.count = 0;
        return count;
    }

    public LauncherManifest getManifest() {
        return manifest;
    }

    public void setManifest(LauncherManifest manifest) {
        this.manifest = manifest;
    }

}
