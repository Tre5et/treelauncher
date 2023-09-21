package net.treset.minecraftlauncher.util;

import javafx.util.Pair;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.fabric.FabricProfile;
import net.treset.mc_version_loader.fabric.FabricUtil;
import net.treset.mc_version_loader.fabric.FabricVersionDetails;
import net.treset.mc_version_loader.json.JsonUtils;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.launcher.LauncherVersionDetails;
import net.treset.mc_version_loader.minecraft.MinecraftUtil;
import net.treset.mc_version_loader.minecraft.MinecraftVersion;
import net.treset.mc_version_loader.minecraft.MinecraftVersionDetails;
import net.treset.mc_version_loader.util.DownloadStatus;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.creation.VersionCreator;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.sync.ComponentData;
import net.treset.minecraftlauncher.sync.ComponentList;
import net.treset.minecraftlauncher.sync.GetResponse;
import net.treset.minecraftlauncher.sync.SyncService;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
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
        CREATING("sync.status.creating"),
        FINISHED("sync.status.finished");
        private final String translationKey;
        SyncStep(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getTranslationKey() {
            return translationKey;
        }
    }

    public static ComponentList getAvailableComponents(LauncherManifestType type) throws IOException {
        SyncService service = new SyncService();
        return service.getAvailable(convertType(type));
    }

    public static void downloadComponent(LauncherManifest component, Consumer<SyncStatus> statusConsumer) throws IOException {
        File dir = new File(component.getDirectory());
        if(!dir.exists()) {
            Files.createDirectories(dir.toPath());
        }
        statusConsumer.accept(new SyncStatus(SyncStep.COLLECTING, null));
        GetResponse response = new SyncService().get(convertType(component.getType()), component.getId(), 0);
        LOGGER.debug("Downloading component: version=" + response.getVersion());
        statusConsumer.accept(new SyncStatus(SyncStep.DOWNLOADING, null));
        getFiles(component.getDirectory(), response.getDifference(), convertType(component.getType()), component.getId(), statusConsumer);
        LOGGER.debug("Download complete");
        updateSyncFile(component, response.getVersion());
        statusConsumer.accept(new SyncStatus(SyncStep.FINISHED, null));
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

    public static void syncComponentFromServer(LauncherManifest component, Consumer<SyncStatus> statusConsumer) throws IOException {
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

    public static void syncComponentToServer(LauncherManifest component, Consumer<SyncStatus> statusConsumer) throws IOException {
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

    public static void syncInstanceToServer(InstanceData instance, Consumer<SyncStatus> statusConsumer) throws IOException {
        statusConsumer.accept(new SyncStatus(SyncStep.COLLECTING, null));
        if(isSyncing(instance.getInstance().getKey())) {
            updateInstance(instance, statusConsumer);
        } else {
            uploadInstance(instance, statusConsumer);
        }
        if(!isSyncing(instance.getResourcepacksComponent())) {
            uploadComponent(instance.getResourcepacksComponent(), statusConsumer);
        }
        if(instance.getModsComponent() != null && !isSyncing(instance.getModsComponent().getKey())) {
            uploadComponent(instance.getModsComponent().getKey(), statusConsumer);
        }
        if(!isSyncing(instance.getOptionsComponent())) {
            uploadComponent(instance.getOptionsComponent(), statusConsumer);
        }
        if(!isSyncing(instance.getSavesComponent())) {
            uploadComponent(instance.getSavesComponent(), statusConsumer);
        }
        statusConsumer.accept(new SyncStatus(SyncStep.FINISHED, null));
    }

    public static void syncInstanceFromServer(InstanceData instance, LauncherFiles files, Consumer<SyncStatus> statusConsumer) throws IOException, FileDownloadException, ComponentCreationException {
        statusConsumer.accept(new SyncStatus(SyncStep.COLLECTING, null));
        SyncService service = new SyncService();

        // TODO: download included files
        File syncFile = new File(FormatUtil.absoluteFilePath(instance.getInstance().getKey().getDirectory(), "data.sync"));
        String componentData = FileUtil.loadFile(syncFile.getPath());
        ComponentData data = ComponentData.fromJson(componentData);
        GetResponse instanceResponse = service.get("instance", instance.getInstance().getKey().getId(), data.getVersion());
        if(instanceResponse.getVersion() == data.getVersion()) {
            LOGGER.debug("Instance is up to date");
            statusConsumer.accept(new SyncStatus(SyncStep.FINISHED, null));
            return;
        }

        statusConsumer.accept(new SyncStatus(SyncStep.DOWNLOADING, new DownloadStatus(1, 2, "instance.json", false)));
        byte[] instanceContent = service.downloadFile("instance", instance.getInstance().getKey().getId(), "instance.json");
        String instanceJson = new String(instanceContent);
        LauncherInstanceDetails instanceDetails = JsonUtils.getGson().fromJson(instanceJson, LauncherInstanceDetails.class);
        statusConsumer.accept(new SyncStatus(SyncStep.DOWNLOADING, new DownloadStatus(2, 2, "version.json", false)));
        byte[] versionContent = service.downloadFile("instance", instance.getInstance().getKey().getId(), "version.json");
        String versionJson = new String(versionContent);
        LauncherVersionDetails versionDetails = JsonUtils.getGson().fromJson(versionJson, LauncherVersionDetails.class);
        syncContents(instance.getInstance().getValue(), instance.getVersionComponents().get(0).getValue(), instance.getInstance().getKey().getTypeConversion(), instanceDetails, versionDetails, files, statusConsumer);
    }

    public static void downloadInstance(String id, LauncherFiles files, Consumer<SyncStatus> statusConsumer) throws IOException, ComponentCreationException, FileDownloadException {
        statusConsumer.accept(new SyncStatus(SyncStep.COLLECTING, null));
        SyncService service = new SyncService();

        GetResponse instanceResponse = service.get("instance", id, 0);

        // TODO: download included files
        statusConsumer.accept(new SyncStatus(SyncStep.DOWNLOADING, new DownloadStatus(1, 3, LauncherApplication.config.MANIFEST_FILE_NAME, false)));
        byte[] manifestContent = service.downloadFile("instance", id, LauncherApplication.config.MANIFEST_FILE_NAME);
        String manifestJson = new String(manifestContent);
        LauncherManifest manifest = JsonUtils.getGson().fromJson(manifestJson, LauncherManifest.class);
        statusConsumer.accept(new SyncStatus(SyncStep.DOWNLOADING, new DownloadStatus(2, 3, "instance.json", false)));
        byte[] instanceContent = service.downloadFile("instance", id, "instance.json");
        String instanceJson = new String(instanceContent);
        LauncherInstanceDetails instanceDetails = JsonUtils.getGson().fromJson(instanceJson, LauncherInstanceDetails.class);
        statusConsumer.accept(new SyncStatus(SyncStep.DOWNLOADING, new DownloadStatus(3, 3, "version.json", false)));
        byte[] versionContent = service.downloadFile("instance", id, "version.json");
        String versionJson = new String(versionContent);
        LauncherVersionDetails versionDetails = JsonUtils.getGson().fromJson(versionJson, LauncherVersionDetails.class);

        LauncherInstanceDetails newDetails = new LauncherInstanceDetails(
                instanceDetails.getFeatures(),
                instanceDetails.getIgnoredFiles(),
                instanceDetails.getJvm_arguments(),
                null,
                null,
                null,
                null,
                null
        );

        syncContents(
                newDetails,
                new LauncherVersionDetails(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                files.getLauncherDetails().getTypeConversion(),
                instanceDetails,
                versionDetails,
                files,
                statusConsumer
        );
        // TODO: create
    }

    private static void syncContents(LauncherInstanceDetails instance, LauncherVersionDetails versionDetails, Map<String, LauncherManifestType> typeConversion, LauncherInstanceDetails newInstance, LauncherVersionDetails newVersion, LauncherFiles files, Consumer<SyncStatus> statusConsumer) throws IOException, FileDownloadException, ComponentCreationException {
        instance.setIgnoredFiles(newInstance.getIgnoredFiles());
        instance.setJvm_arguments(newInstance.getJvm_arguments());
        if(files.getSavesManifest().getComponents().contains(newInstance.getSavesComponent())) {
            instance.setSavesComponent(newInstance.getSavesComponent());
        } else {
            LauncherManifest fakeManifest = new LauncherManifest(
                    FormatUtil.getStringFromType(LauncherManifestType.SAVES_COMPONENT, files.getLauncherDetails().getTypeConversion()),
                    files.getLauncherDetails().getTypeConversion(),
                    newInstance.getSavesComponent(),
                    null,
                    null,
                    null,
                    null,
                    null

            );
            fakeManifest.setDirectory(FormatUtil.absoluteFilePath(files.getSavesManifest().getDirectory(), files.getSavesManifest().getPrefix() + "_" + newInstance.getSavesComponent()));
            downloadComponent(fakeManifest, statusConsumer);
            instance.setSavesComponent(fakeManifest.getId());
        }
        if(files.getOptionsManifest().getComponents().contains(newInstance.getOptionsComponent())) {
            instance.setOptionsComponent(newInstance.getOptionsComponent());
        } else {
            LauncherManifest fakeManifest = new LauncherManifest(
                    FormatUtil.getStringFromType(LauncherManifestType.OPTIONS_COMPONENT, files.getLauncherDetails().getTypeConversion()),
                    files.getLauncherDetails().getTypeConversion(),
                    newInstance.getOptionsComponent(),
                    null,
                    null,
                    null,
                    null,
                    null
            );
            fakeManifest.setDirectory(FormatUtil.absoluteFilePath(files.getOptionsManifest().getDirectory(), files.getOptionsManifest().getPrefix() + "_" + newInstance.getOptionsComponent()));
            downloadComponent(fakeManifest, statusConsumer);
            instance.setOptionsComponent(fakeManifest.getId());
        }
        if(files.getResourcepackManifest().getComponents().contains(newInstance.getResourcepacksComponent())) {
            instance.setResourcepacksComponent(newInstance.getResourcepacksComponent());
        } else {
            LauncherManifest fakeManifest = new LauncherManifest(
                    FormatUtil.getStringFromType(LauncherManifestType.RESOURCEPACKS_COMPONENT, files.getLauncherDetails().getTypeConversion()),
                    files.getLauncherDetails().getTypeConversion(),
                    newInstance.getResourcepacksComponent(),
                    null,
                    null,
                    null,
                    null,
                    null
            );
            fakeManifest.setDirectory(FormatUtil.absoluteFilePath(files.getResourcepackManifest().getDirectory(), files.getResourcepackManifest().getPrefix() + "_" + newInstance.getResourcepacksComponent()));
            downloadComponent(fakeManifest, statusConsumer);
            instance.setResourcepacksComponent(fakeManifest.getId());
        }
        if(newInstance.getModsComponent() != null) {
            if(files.getModsManifest().getComponents().contains(newInstance.getModsComponent())) {
                instance.setModsComponent(newInstance.getModsComponent());
            } else {
                LauncherManifest fakeManifest = new LauncherManifest(
                        FormatUtil.getStringFromType(LauncherManifestType.MODS_COMPONENT, files.getLauncherDetails().getTypeConversion()),
                        files.getLauncherDetails().getTypeConversion(),
                        newInstance.getModsComponent(),
                        null,
                        null,
                        null,
                        null,
                        null
                );
                fakeManifest.setDirectory(FormatUtil.absoluteFilePath(files.getModsManifest().getDirectory(), files.getModsManifest().getPrefix() + "_" + newInstance.getModsComponent()));
                downloadComponent(fakeManifest, statusConsumer);
                instance.setModsComponent(fakeManifest.getId());
            }
        }

        if(!Objects.equals(versionDetails.getVersionId(), newVersion.getVersionId())) {
            statusConsumer.accept(new SyncStatus(SyncStep.CREATING, null));
            Optional<MinecraftVersion> version = MinecraftUtil.getReleases().stream().filter(e -> e.getId().equals(newVersion.getVersionId())).findFirst();
            if(version.isEmpty()) {
                throw new FileDownloadException("Failed to find version: " + newVersion.getVersionId());
            }
            MinecraftVersionDetails details = MinecraftUtil.getVersionDetails(version.get().getUrl());
            VersionCreator creator;
            if(Objects.equals(newVersion.getVersionType(), "vanilla")) {
                creator = new VersionCreator(
                        typeConversion,
                        files.getVersionManifest(),
                        details,
                        files,
                        FormatUtil.absoluteDirPath(LauncherApplication.config.BASE_DIR, files.getLauncherDetails().getLibrariesDir())
                );
            } else {
                FabricVersionDetails fabricDetails = FabricUtil.getFabricVersionDetails(newVersion.getVersionNumber(), newVersion.getLoaderVersion());
                FabricProfile profile = FabricUtil.getFabricProfile(newVersion.getVersionNumber(), newVersion.getLoaderVersion());
                creator = new VersionCreator(
                        typeConversion,
                        files.getVersionManifest(),
                        fabricDetails,
                        profile,
                        files,
                        FormatUtil.absoluteDirPath(LauncherApplication.config.BASE_DIR, files.getLauncherDetails().getLibrariesDir())
                );
            }
            creator.setStatusCallback((status) -> {
                statusConsumer.accept(new SyncStatus(SyncStep.CREATING, status.getDownloadStatus()));
            });
            String id = creator.createComponent();
            instance.setVersionComponent(id);
        }
    }

    private static void uploadInstance(InstanceData instance, Consumer<SyncStatus> statusConsumer) throws IOException {
        statusConsumer.accept(new SyncStatus(SyncStep.UPLOADING, null));
        SyncService service = new SyncService();
        service.newComponent("instance", instance.getInstance().getKey().getId());
        updateInstance(instance, statusConsumer);
    }

    private static void updateInstance(InstanceData instance, Consumer<SyncStatus> statusConsumer) throws IOException {
        statusConsumer.accept(new SyncStatus(SyncStep.UPLOADING, null));
        SyncService service = new SyncService();
        LauncherInstanceDetails newDetails = new LauncherInstanceDetails(
                null,
                instance.getInstance().getValue().getIgnoredFiles(),
                instance.getInstance().getValue().getJvm_arguments(),
                instance.getInstance().getValue().getModsComponent(),
                instance.getInstance().getValue().getOptionsComponent(),
                instance.getInstance().getValue().getResourcepacksComponent(),
                instance.getInstance().getValue().getSavesComponent(),
                null
        );
        LauncherVersionDetails versionDetails = new LauncherVersionDetails(
                instance.getVersionComponents().get(0).getValue().getVersionNumber(),
                instance.getVersionComponents().get(0).getValue().getVersionType(),
                instance.getVersionComponents().get(0).getValue().getLoaderVersion(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        statusConsumer.accept(new SyncStatus(SyncStep.UPLOADING, new DownloadStatus(1, 3, LauncherApplication.config.MANIFEST_FILE_NAME, false)));
        service.uploadFile("instance", instance.getInstance().getKey().getId(), LauncherApplication.config.MANIFEST_FILE_NAME, FileUtil.readFile(FormatUtil.absoluteFilePath(instance.getInstance().getKey().getDirectory(), LauncherApplication.config.MANIFEST_FILE_NAME)));
        statusConsumer.accept(new SyncStatus(SyncStep.UPLOADING, new DownloadStatus(2, 3, "instance.json", false)));
        service.uploadFile("instance", instance.getInstance().getKey().getId(), "instance.json", JsonUtils.getGson().toJson(newDetails).getBytes());
        statusConsumer.accept(new SyncStatus(SyncStep.UPLOADING, new DownloadStatus(3, 3, "version.json", false)));
        service.uploadFile("instance", instance.getInstance().getKey().getId(), "version.json", JsonUtils.getGson().toJson(versionDetails).getBytes());

        // TODO: upload Included Files

        int versionNumber = service.complete("instance", instance.getInstance().getKey().getId());
        ComponentData data = new ComponentData(versionNumber, 3, null);
        data.writeToFile(FormatUtil.absoluteFilePath(instance.getInstance().getKey().getDirectory(), "data.sync"));
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
        int amount = 0;
        for(String path : difference) {
            path = FormatUtil.urlDecode(path);
            LOGGER.debug("Downloading file: " + path);
            statusConsumer.accept(new SyncStatus(SyncStep.DOWNLOADING, new DownloadStatus(++amount, difference.size(), path, false)));
            byte[] content = service.downloadFile(type, id, path);
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
