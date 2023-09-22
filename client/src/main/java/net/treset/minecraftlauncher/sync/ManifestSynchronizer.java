package net.treset.minecraftlauncher.sync;

import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.util.DownloadStatus;
import net.treset.minecraftlauncher.util.FileUtil;
import net.treset.minecraftlauncher.util.FormatUtil;
import net.treset.minecraftlauncher.util.SyncUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ManifestSynchronizer extends FileSynchronizer {
    private LauncherManifest manifest;

    public ManifestSynchronizer(LauncherManifest manifest, SyncCallback callback) {
        super(callback);
        this.manifest = manifest;
    }

    @Override
    public void upload() throws IOException {
        if(SyncUtil.isSyncing(manifest)) {
            uploadExisting();
        } else {
            uploadNew();
        }

    }

    @Override
    public void download() throws IOException {
        if(SyncUtil.isSyncing(manifest)) {
            downloadExisting();
        } else {
            downloadNew();
        }
    }

    protected void uploadExisting() throws IOException {
        setStatus(new SyncStatus(SyncStep.COLLECTING, null));
        ComponentData currentData = getCurrentComponentData();
        ComponentData newData = calculateComponentData(currentData.getVersion());
        List<String> difference = compareHashes(new ComponentData.HashEntry("", currentData.getHashTree()), new ComponentData.HashEntry("", newData.getHashTree()), null);
        uploadDifference(difference);
        completeUpload(newData);
        setStatus(new SyncStatus(SyncStep.FINISHED, null));
    }

    protected void uploadNew() throws IOException {
        setStatus(new SyncStatus(SyncStep.STARTING, null));
        LOGGER.debug("Sync file not found, creating new sync file");
        setStatus(new SyncStatus(SyncStep.COLLECTING, null));
        ComponentData data = updateSyncFile(0);
        setStatus(new SyncStatus(SyncStep.UPLOADING, null));
        SyncService service = new SyncService();
        service.newComponent(SyncUtil.convertType(manifest.getType()), manifest.getId());
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
    }

    protected void uploadAll(ComponentData data) throws IOException {
        SyncService service = new SyncService();
        uploadDirectory(data.getHashTree(), SyncUtil.convertType(manifest.getType()), manifest.getId(), manifest.getDirectory(), "", 0, data.getFileAmount(), service);
    }

    protected Integer uploadDirectory(List<ComponentData.HashEntry> entries, String type, String id, String basePath, String filePath, Integer currentAmount, Integer totalAmount, SyncService service) throws IOException {
        for(ComponentData.HashEntry child : entries) {
            if(child.getChildren() == null) {
                LOGGER.debug("Uploading file: " + child.getPath());
                currentAmount++;
                setStatus(new SyncStatus(SyncStep.UPLOADING, new DownloadStatus(currentAmount, totalAmount, child.getPath(), false)));
                File file = new File(FormatUtil.absoluteFilePath(basePath, filePath, child.getPath()));
                byte[] content = FileUtil.readFile(file.getPath());
                service.uploadFile(type, id, FormatUtil.absoluteFilePath(filePath, child.getPath()), content);
            } else {
                currentAmount = uploadDirectory(child.getChildren(), type, id, basePath, FormatUtil.absoluteDirPath(filePath + child.getPath()), currentAmount, totalAmount, service);
            }
        }
        return currentAmount;
    }

    protected void uploadDifference(List<String> difference) throws IOException {
        setStatus(new SyncStatus(SyncStep.UPLOADING, null));
        SyncService syncService = new SyncService();
        int index = 0;
        for(String path : difference) {
            index++;
            setStatus(new SyncStatus(SyncStep.UPLOADING, new DownloadStatus(index, difference.size(), path, false)));
            LOGGER.debug("Difference: " + path);
            File file = new File(FormatUtil.absoluteFilePath(manifest.getDirectory(), path));
            byte[] content;
            if(file.isFile()) {
                content = FileUtil.readFile(file.getPath());
            } else {
                content = new byte[]{};
            }
            syncService.uploadFile(SyncUtil.convertType(manifest.getType()), manifest.getId(), path, content);
        }
    }

    protected int completeUpload(ComponentData newData) throws IOException {
        int version = new SyncService().complete(SyncUtil.convertType(manifest.getType()), manifest.getId());
        newData.setVersion(version);
        newData.writeToFile(FormatUtil.absoluteFilePath(manifest.getDirectory(), SyncUtil.SYNC_FILENAME));
        return version;
    }

    protected List<String> compareHashes(ComponentData.HashEntry oldEntry, ComponentData.HashEntry newEntry, String path) {
        ArrayList<String> difference = new ArrayList<>();
        if(oldEntry.getChildren() == null && newEntry.getChildren() == null) {
            if(!oldEntry.getHash().equals(newEntry.getHash())) {
                LOGGER.debug("Adding changed file: " + path);
                difference.add(path);
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
                            difference.addAll(getAllChildren(newEntry.getChildren().get(l), FormatUtil.absoluteFilePath(path, newEntry.getChildren().get(l).getPath())));
                        }
                        difference.addAll(compareHashes(oldEntry.getChildren().get(i), newEntry.getChildren().get(k), FormatUtil.absoluteFilePath(path, oldEntry.getChildren().get(i).getPath())));
                        j = k+1;
                        break;
                    }
                }
                if(!found) {
                    LOGGER.debug("Adding deleted file: " + oldEntry.getChildren().get(i).getPath());
                    difference.addAll(getAllChildren(oldEntry.getChildren().get(i), FormatUtil.absoluteFilePath(path,  oldEntry.getChildren().get(i).getPath())));
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

    protected List<String> getAllChildren(ComponentData.HashEntry entry, String path) {
        ArrayList<String> children = new ArrayList<>();
        if(entry.getChildren() == null) {
            children.add(path);
        } else {
            for(ComponentData.HashEntry child : entry.getChildren()) {
                children.addAll(getAllChildren(child, FormatUtil.absoluteFilePath(path, child.getPath())));
            }
        }
        return children;
    }

    protected ComponentData getCurrentComponentData() throws IOException {
        File syncFile = new File(FormatUtil.absoluteFilePath(manifest.getDirectory(), SyncUtil.SYNC_FILENAME));
        if(!syncFile.exists()) {
            throw new IOException("Sync file not found");
        }
        String componentData = FileUtil.loadFile(syncFile.getPath());
        return ComponentData.fromJson(componentData);
    }

    protected void downloadDifference(int currentVersion) throws IOException {
        setStatus(new SyncStatus(SyncStep.COLLECTING, null));
        GetResponse response = new SyncService().get(SyncUtil.convertType(manifest.getType()), manifest.getId(), currentVersion);
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
        String type = SyncUtil.convertType(manifest.getType());
        String basePath = manifest.getDirectory();
        int amount = 0;
        for(String path : difference) {
            path = FormatUtil.urlDecode(path);
            LOGGER.debug("Downloading file: " + path);
            setStatus(new SyncStatus(SyncStep.DOWNLOADING, new DownloadStatus(++amount, difference.size(), path, false)));
            byte[] content = service.downloadFile(type, manifest.getId(), path);
            File file = new File(FormatUtil.absoluteFilePath(basePath, path));
            if(content.length == 0 && file.exists()) {
                LOGGER.debug("Deleting file: " + path);
                FileUtil.deleteFile(FormatUtil.absoluteFilePath(basePath, path));
            } else {
                if(!file.isFile())  {
                    if(file.isDirectory()) {
                        FileUtil.deleteDir(file);
                    }
                    Files.createDirectories(file.toPath().getParent());
                    Files.createFile(file.toPath());
                }
                FileUtil.writeFile(FormatUtil.absoluteFilePath(basePath, path), content);
            }
        }
    }

    protected ComponentData updateSyncFile(int version) throws IOException {
        ComponentData data = calculateComponentData(version);
        data.writeToFile(FormatUtil.absoluteFilePath(manifest.getDirectory(), SyncUtil.SYNC_FILENAME));
        return data;
    }

    protected ComponentData calculateComponentData(int version) throws IOException {
        LOGGER.debug("Collecting component data for component: " + manifest.getId());
        long startTime = System.currentTimeMillis();
        File componentDir = new File(manifest.getDirectory());
        Pair<Integer, List<ComponentData.HashEntry>> result = hashDirectoryContents(componentDir, 0);
        LOGGER.debug("Component data collected in " + (System.currentTimeMillis() - startTime) + "ms");
        return new ComponentData(version, result.getKey(), result.getValue());
    }

    protected Pair<Integer, List<ComponentData.HashEntry>> hashDirectoryContents(File dir, Integer fileAmount) throws IOException {
        File[] children = dir.listFiles((dir1, name) -> !name.equals("data.sync") && !name.equals(".included_files_old"));
        if(children == null) return new Pair<>(fileAmount, List.of());
        ArrayList<ComponentData.HashEntry> hashTree = new ArrayList<>();
        for(File file : children) {
            if(file.isDirectory()) {
                Pair<Integer, List<ComponentData.HashEntry>> result = hashDirectoryContents(file, fileAmount);
                fileAmount = result.getKey();
                hashTree.add(new ComponentData.HashEntry(file.getName(), result.getValue()));
            } else if(file.isFile()) {
                fileAmount++;
                hashTree.add(new ComponentData.HashEntry(file.getName(), FormatUtil.hashFile(file)));
            }
        }
        return new Pair<>(fileAmount, hashTree);
    }

    public LauncherManifest getManifest() {
        return manifest;
    }

    public void setManifest(LauncherManifest manifest) {
        this.manifest = manifest;
    }

}
