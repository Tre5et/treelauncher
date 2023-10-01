package net.treset.minecraftlauncher.sync;

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
import net.treset.minecraftlauncher.util.FormatUtil;
import net.treset.minecraftlauncher.util.SyncUtil;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class InstanceSynchronizer extends ManifestSynchronizer {
    private InstanceData instanceData;

    private boolean updateEverything = false;

    public InstanceSynchronizer(InstanceData instanceData, LauncherFiles files, SyncCallback callback) {
        super(instanceData.getInstance().getKey(), files, callback);
        this.instanceData = instanceData;
    }

    public InstanceSynchronizer(InstanceData instanceData, LauncherFiles files, boolean updateEverything, SyncCallback callback) {
        super(instanceData.getInstance().getKey(), files, callback);
        this.instanceData = instanceData;
        this.updateEverything = updateEverything;
    }

    @Override
    public void upload() throws IOException {
        super.upload();
        uploadDependency(instanceData.getSavesComponent());
        uploadDependency(instanceData.getResourcepacksComponent());
        uploadDependency(instanceData.getOptionsComponent());
        if(instanceData.getModsComponent() != null) {
            uploadDependency(instanceData.getModsComponent().getKey());
        }
    }

    @Override
    public void download() throws IOException {
        super.download();
    }

    @Override
    protected void uploadAll(ComponentData data) throws IOException {
        uploadVersionFile();
        super.uploadAll(data);
    }

    @Override
    protected void uploadDifference(List<String> difference) throws IOException {
        uploadVersionFile();
        super.uploadDifference(difference);
    }

    @Override
    protected void downloadNew() throws IOException {
        super.downloadNew();
    }

    @Override
    protected void downloadFiles(List<String> difference) throws IOException {
        String detailsFileName = null;
        ArrayList<String> newDifference = new ArrayList<>();
        for(String file : difference) {
            if(file.equals("version.json")) {
                try {
                    downloadVersion();
                } catch (Exception e) {
                    throw new IOException("Failed to download version file", e);
                }
            } else if(file.equals(LauncherApplication.config.MANIFEST_FILE_NAME) || file.equals(instanceData.getInstance().getKey().getDetails())) {
                if(detailsFileName == null) {
                    detailsFileName = downloadDetails();
                }
            } else {
                newDifference.add(file);
            }
        }
        if(detailsFileName != null) {
            instanceData.getInstance().getValue().writeToFile(FormatUtil.absoluteFilePath(instanceData.getInstance().getKey().getDirectory(), detailsFileName));
            newDifference.remove(detailsFileName);
        }
        super.downloadFiles(newDifference);
    }

    protected String downloadDetails() throws IOException {
        String details = downloadManifest();
        SyncService service = new SyncService();
        byte[] out = service.downloadFile("instance", instanceData.getInstance().getKey().getId(), details);
        LauncherInstanceDetails newDetails = JsonUtils.getGson().fromJson(new String(out), LauncherInstanceDetails.class);
        instanceData.getInstance().getValue().setFeatures(newDetails.getFeatures());
        instanceData.getInstance().getValue().setJvm_arguments(newDetails.getJvm_arguments());
        instanceData.getInstance().getValue().setIgnoredFiles(newDetails.getIgnoredFiles());
        instanceData.getInstance().getValue().setLastPlayed(newDetails.getLastPlayed());
        instanceData.getInstance().getValue().setTotalTime(newDetails.getTotalTime());

        instanceData.getInstance().getValue().setSavesComponent(
                downloadDependency(newDetails.getSavesComponent(), LauncherManifestType.SAVES_COMPONENT)
        );
        instanceData.getInstance().getValue().setResourcepacksComponent(
                downloadDependency(newDetails.getResourcepacksComponent(), LauncherManifestType.RESOURCEPACKS_COMPONENT)
        );
        instanceData.getInstance().getValue().setOptionsComponent(
                downloadDependency(newDetails.getOptionsComponent(), LauncherManifestType.OPTIONS_COMPONENT)
        );
        instanceData.getInstance().getValue().setModsComponent(
                downloadDependency(newDetails.getModsComponent(), LauncherManifestType.MODS_COMPONENT)
        );

        return details;
    }

    protected String downloadManifest() throws IOException {
        SyncService service = new SyncService();
        byte[] out = service.downloadFile("instance", instanceData.getInstance().getKey().getId(), LauncherApplication.config.MANIFEST_FILE_NAME);
        LauncherManifest manifest = JsonUtils.getGson().fromJson(new String(out), LauncherManifest.class);
        manifest.writeToFile(FormatUtil.absoluteFilePath(instanceData.getInstance().getKey().getDirectory(), LauncherApplication.config.MANIFEST_FILE_NAME));
        return manifest.getDetails();
    }

    protected String downloadDependency(String id, LauncherManifestType type) throws IOException {
        LauncherManifest parentManifest;
        List<LauncherManifest> otherComponents;
        LauncherManifest currentManifest;
        switch (type) {
            case SAVES_COMPONENT -> {
                parentManifest = files.getSavesManifest();
                otherComponents = files.getSavesComponents();
                currentManifest = instanceData.getSavesComponent();
            }
            case RESOURCEPACKS_COMPONENT -> {
                parentManifest = files.getResourcepackManifest();
                otherComponents = files.getResourcepackComponents();
                currentManifest = instanceData.getResourcepacksComponent();
            }
            case OPTIONS_COMPONENT -> {
                parentManifest = files.getOptionsManifest();
                otherComponents = files.getOptionsComponents();
                currentManifest = instanceData.getOptionsComponent();
            }
            case MODS_COMPONENT -> {
                parentManifest = files.getModsManifest();
                otherComponents = files.getModsComponents().stream().map(Pair::getKey).toList();
                currentManifest = instanceData.getModsComponent() == null ? null : instanceData.getModsComponent().getKey();
            }
            default -> throw new IOException("Unknown component type: " + type);
        }
        if(id != null && (currentManifest == null || !id.equals(currentManifest.getId()))) {
            LauncherManifest existingManifest = otherComponents.stream()
                    .filter((manifest) -> manifest.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            if(existingManifest != null) {
                if(updateEverything) {
                    ManifestSynchronizer synchronizer = new ManifestSynchronizer(existingManifest, files, callback);
                    synchronizer.download();
                }
            } else {
                LauncherManifest fakeManifest = new LauncherManifest(
                        FormatUtil.getStringFromType(type, files.getLauncherDetails().getTypeConversion()),
                        files.getLauncherDetails().getTypeConversion(),
                        id,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                fakeManifest.setDirectory(FormatUtil.absoluteDirPath(parentManifest.getDirectory(), parentManifest.getPrefix() + "_" + id));
                ManifestSynchronizer synchronizer = new ManifestSynchronizer(fakeManifest, files, callback);
                synchronizer.download();
            }
        }
        return id;
    }

    protected void downloadVersion() throws IOException, ComponentCreationException, FileDownloadException {
        SyncService service = new SyncService();
        setStatus(new SyncStatus(SyncStep.DOWNLOADING, new DownloadStatus(0, 0, "version.json", false)));
        LauncherVersionDetails details = JsonUtils.getGson().fromJson(new String(service.downloadFile("instance", instanceData.getInstance().getKey().getId(), "version.json")), LauncherVersionDetails.class);
        VersionCreator creator;
        if(instanceData.getVersionComponents() == null || !Objects.equals(details.getVersionId(), instanceData.getVersionComponents().get(0).getValue().getVersionId())) {
            if("vanilla".equals(details.getVersionType())) {
                creator = getVanillaCreator(details);
            } else if("fabric".equals(details.getVersionType())) {
                creator = getFabricCreator(details);
            } else {
                throw new IOException("Getting version returned unknown version type: " + details.getVersionType());
            }
            creator.setStatusCallback((status) -> setStatus(new SyncStatus(SyncStep.CREATING, status.getDownloadStatus())));
            String id = creator.createComponent();
            instanceData.getInstance().getValue().setVersionComponent(id);
        }
    }

    protected VersionCreator getVanillaCreator(LauncherVersionDetails details) throws IOException, FileDownloadException {
        Optional<MinecraftVersion> version = MinecraftUtil.getReleases().stream()
                .filter(v -> v.getId().equals(details.getVersionId()) || v.getId().equals(details.getVersionNumber()))
                .findFirst();
        if(version.isEmpty()) {
            throw new IOException("Failed to find version: " + details.getVersionId());
        }
        String url = version.get().getUrl();
        MinecraftVersionDetails versionDetails = MinecraftUtil.getVersionDetails(url);
        return new VersionCreator(
                instanceData.getInstance().getKey().getTypeConversion(),
                files.getVersionManifest(),
                versionDetails,
                files,
                FormatUtil.absoluteDirPath(files.getMainManifest().getDirectory(), files.getLauncherDetails().getLibrariesDir())
        );
    }

    protected VersionCreator getFabricCreator(LauncherVersionDetails details) throws IOException, FileDownloadException {
        FabricVersionDetails version = FabricUtil.getFabricVersionDetails(details.getVersionNumber(), details.getLoaderVersion());
        FabricProfile profile = FabricUtil.getFabricProfile(details.getVersionNumber(), details.getLoaderVersion());
        return new VersionCreator(
                instanceData.getInstance().getKey().getTypeConversion(),
                files.getVersionManifest(),
                version,
                profile,
                files,
                FormatUtil.absoluteDirPath(files.getMainManifest().getDirectory(), files.getLauncherDetails().getLibrariesDir())
        );
    }

    protected void uploadDependency(LauncherManifest manifest) throws IOException {
        if(manifest != null && (updateEverything || !SyncUtil.isSyncing(manifest))) {
            ManifestSynchronizer synchronizer = new ManifestSynchronizer(manifest, files, callback);
            synchronizer.upload();
        }
    }

    protected void uploadVersionFile() throws IOException {
        LauncherVersionDetails details = new LauncherVersionDetails(
                instanceData.getVersionComponents().get(0).getValue().getVersionNumber(),
                instanceData.getVersionComponents().get(0).getValue().getVersionType(),
                instanceData.getVersionComponents().get(0).getValue().getLoaderVersion(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                instanceData.getVersionComponents().get(0).getValue().getVersionId()
        );

        SyncService service = new SyncService();
        setStatus(new SyncStatus(SyncStep.UPLOADING, new DownloadStatus(0, 0, "version.json", false)));
        service.uploadFile("instance", instanceData.getInstance().getKey().getId(), "version.json", JsonUtils.getGson().toJson(details).getBytes());
    }

    public InstanceData getInstanceData() {
        return instanceData;
    }

    public void setInstanceData(InstanceData instanceData) {
        this.instanceData = instanceData;
    }

    public boolean isUpdateEverything() {
        return updateEverything;
    }

    public void setUpdateEverything(boolean updateEverything) {
        this.updateEverything = updateEverything;
    }
}
