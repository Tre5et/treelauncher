package net.treset.minecraftlauncher.util;

import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.util.DownloadStatus;
import net.treset.minecraftlauncher.sync.ComponentData;
import net.treset.minecraftlauncher.sync.GetResponse;
import net.treset.minecraftlauncher.sync.SyncService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SyncUtil {
    private static final Logger LOGGER = LogManager.getLogger(SyncUtil.class);

    public static class SyncStatus {
        private final SyncStep step;
        private final DownloadStatus status;

        public SyncStatus(SyncStep step, DownloadStatus status) {
            this.step = step;
            this.status = status;
        }

        public SyncStep getStep() {
            return step;
        }

        public DownloadStatus getStatus() {
            return status;
        }
    }

    public enum SyncStep {
        STARTING("sync.status.starting"),
        COLLECTING("sync.status.collecting"),
        UPLOADING("sync.status.uploading"),
        DOWNLOADING("sync.status.downloading"),
        FINISHED("sync.status.finished");
        private final String translationKey;
        SyncStep(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getTranslationKey() {
            return translationKey;
        }
    }

    public static void uploadComponent(LauncherManifest component, Consumer<SyncStatus> statusConsumer) throws IOException {
        statusConsumer.accept(new SyncStatus(SyncStep.STARTING, null));
        LOGGER.debug("Sync file not found, creating new sync file");
        statusConsumer.accept(new SyncStatus(SyncStep.COLLECTING, null));
        ComponentData data = updateSyncFile(component, 0);
        statusConsumer.accept(new SyncStatus(SyncStep.UPLOADING, null));
        SyncService service = new SyncService();
        LauncherManifestType manifestType = component.getType();
        service.newComponent(convertType(manifestType), component.getId());
        uploadAll(data, convertType(manifestType), component.getId(), component.getDirectory(), statusConsumer);
        LOGGER.debug("Completing upload...");
        int newVersion = service.complete(convertType(manifestType), component.getId());
        data.setVersion(newVersion);
        data.writeToFile(FormatUtil.absoluteFilePath(component.getDirectory(), "data.sync"));
        LOGGER.debug("Upload complete: version=" + newVersion);
        statusConsumer.accept(new SyncStatus(SyncStep.FINISHED, null));
    }

    public static void updateComponent(LauncherManifest component, Consumer<SyncStatus> statusConsumer) throws IOException {
        File syncFile = new File(FormatUtil.absoluteFilePath(component.getDirectory(), "data.sync"));
        if(!syncFile.exists()) {
            throw new IOException("Sync file not found");
        }
        String componentData = FileUtil.loadFile(syncFile.getPath());
        ComponentData data = ComponentData.fromJson(componentData);

        statusConsumer.accept(new SyncStatus(SyncStep.COLLECTING, null));

        GetResponse response = new SyncService().get(convertType(component.getType()), component.getId(), data.getVersion());
        if(response.getVersion() == data.getVersion()) {
            LOGGER.debug("Component is up to date");
            statusConsumer.accept(new SyncStatus(SyncStep.FINISHED, null));
            return;
        }

        LOGGER.debug("Updating component: version=" + data.getVersion() + " -> " + response.getVersion());
        statusConsumer.accept(new SyncStatus(SyncStep.DOWNLOADING, null));

        getFiles(component.getDirectory(), response.getDifference(), convertType(component.getType()), component.getId(), statusConsumer);

        LOGGER.debug("Updating sync file");
        statusConsumer.accept(new SyncStatus(SyncStep.COLLECTING, null));
        updateSyncFile(component, response.getVersion());
        LOGGER.debug("Update complete");
        statusConsumer.accept(new SyncStatus(SyncStep.FINISHED, null));
    }

    public static void syncComponent(LauncherManifest component, Consumer<SyncStatus> statusConsumer) throws IOException {
        statusConsumer.accept(new SyncStatus(SyncStep.COLLECTING, null));
        File syncFile = new File(FormatUtil.absoluteFilePath(component.getDirectory(), "data.sync"));
        String componentData = FileUtil.loadFile(syncFile.getPath());
        ComponentData data = ComponentData.fromJson(componentData);
        ComponentData newData = getComponentData(component, data.getVersion());
        List<String> difference = compareHashes(new ComponentData.HashEntry("", data.getHashTree()), new ComponentData.HashEntry("", newData.getHashTree()), null);
        statusConsumer.accept(new SyncStatus(SyncStep.UPLOADING, null));
        SyncService syncService = new SyncService();
        int index = 0;
        for(String path : difference) {
            index++;
            statusConsumer.accept(new SyncStatus(SyncStep.UPLOADING, new DownloadStatus(index, difference.size(), path, false)));
            LOGGER.debug("Difference: " + path);
            File file = new File(FormatUtil.absoluteFilePath(component.getDirectory(), path));
            byte[] content;
            if(file.isFile()) {
                content = FileUtil.readFile(file.getPath());
            } else {
                content = new byte[]{};
            }
            syncService.uploadFile(convertType(component.getType()), component.getId(), path, content);
        }
        int version = syncService.complete(convertType(component.getType()), component.getId());
        newData.setVersion(version);
        newData.writeToFile(FormatUtil.absoluteFilePath(component.getDirectory(), "data.sync"));
        statusConsumer.accept(new SyncStatus(SyncStep.FINISHED, null));
    }

    private static List<String> compareHashes(ComponentData.HashEntry oldEntry, ComponentData.HashEntry newEntry, String path) {
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

    private static List<String> getAllChildren(ComponentData.HashEntry entry, String path) {
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

    public static boolean isSyncing(LauncherManifest component) {
        return new File(FormatUtil.absoluteFilePath(component.getDirectory(), "data.sync")).exists();
    }

    private static void getFiles(String basePath, List<String> difference, String type, String id, Consumer<SyncStatus> statusConsumer) throws IOException {
        SyncService service = new SyncService();
        for(String path : difference) {
            path = FormatUtil.urlDecode(path);
            LOGGER.debug("Downloading file: " + path);
            statusConsumer.accept(new SyncStatus(SyncStep.DOWNLOADING, new DownloadStatus(0, difference.size(), path, false)));
            byte[] content = service.downloadFile(type, id, path);
            if(content.length == 0 && new File(FormatUtil.absoluteFilePath(basePath, path)).exists()) {
                LOGGER.debug("Deleting file: " + path);
                FileUtil.deleteFile(FormatUtil.absoluteFilePath(basePath, path));
            }
            FileUtil.writeFile(FormatUtil.absoluteFilePath(basePath, path), content);
        }
    }

    private static void uploadAll(ComponentData data, String type, String id, String basePath, Consumer<SyncStatus> statusConsumer) throws IOException {
        SyncService service = new SyncService();
        uploadDirectory(data.getHashTree(), type, id, basePath, "", 0, data.getFileAmount(), service, statusConsumer);
    }

    private static Integer uploadDirectory(List<ComponentData.HashEntry> entries, String type, String id, String basePath, String filePath, Integer currentAmount, Integer totalAmount, SyncService service, Consumer<SyncStatus> statusConsumer) throws IOException {
        for(ComponentData.HashEntry child : entries) {
            if(child.getChildren() == null) {
                LOGGER.debug("Uploading file: " + child.getPath());
                currentAmount++;
                statusConsumer.accept(new SyncStatus(SyncStep.UPLOADING, new DownloadStatus(currentAmount, totalAmount, child.getPath(), false)));
                File file = new File(FormatUtil.absoluteFilePath(basePath, filePath, child.getPath()));
                byte[] content = FileUtil.readFile(file.getPath());
                service.uploadFile(type, id, FormatUtil.absoluteFilePath(filePath, child.getPath()), content);
            } else {
                currentAmount = uploadDirectory(child.getChildren(), type, id, basePath, FormatUtil.absoluteDirPath(filePath + child.getPath()), currentAmount, totalAmount, service, statusConsumer);
            }
        }
        return currentAmount;
    }

    private static ComponentData updateSyncFile(LauncherManifest component, int version) throws IOException {
        ComponentData data = getComponentData(component, version);
        data.writeToFile(FormatUtil.absoluteFilePath(component.getDirectory(), "data.sync"));
        return data;
    }

    private static ComponentData getComponentData(LauncherManifest component, int version) throws IOException {
        LOGGER.debug("Collecting component data for component: " + component.getId());
        long startTime = System.currentTimeMillis();
        File componentDir = new File(component.getDirectory());
        Pair<Integer, List<ComponentData.HashEntry>> result = hashDirectoryContents(componentDir, 0);
        LOGGER.debug("Component data collected in " + (System.currentTimeMillis() - startTime) + "ms");
        return new ComponentData(version, result.getKey(), result.getValue());
    }

    private static Pair<Integer, List<ComponentData.HashEntry>> hashDirectoryContents(File dir, Integer fileAmount) throws IOException {
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

    private static String convertType(LauncherManifestType type) {
        return switch (type) {
            case RESOURCEPACKS_COMPONENT -> "resourcepacks";
            case MODS_COMPONENT -> "mods";
            case OPTIONS_COMPONENT -> "options";
            case SAVES_COMPONENT -> "saves";
            case INSTANCE_COMPONENT -> "instance";
            default -> "unknown";
        };
    }
}
