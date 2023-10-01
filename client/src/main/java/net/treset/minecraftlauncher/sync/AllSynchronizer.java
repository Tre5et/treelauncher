package net.treset.minecraftlauncher.sync;

import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherModsDetails;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.util.SyncUtil;
import net.treset.minecraftlauncher.util.exception.FileLoadException;

import java.io.IOException;

public class AllSynchronizer extends FileSynchronizer {
    private final LauncherFiles files;

    public AllSynchronizer(LauncherFiles files, SyncCallback callback) {
        super(callback);
        this.files = files;
    }

    @Override
    public void upload() throws IOException {
        for(Pair<LauncherManifest, LauncherInstanceDetails> details : files.getInstanceComponents()) {
            if(!SyncUtil.isSyncing(details.getKey())) {
                continue;
            }
            InstanceData data;
            try {
                data = InstanceData.of(details, files);
            } catch (FileLoadException e) {
                throw new IOException(e);
            }
            new InstanceSynchronizer(data, files, callback).upload();
        }
        for(LauncherManifest manifest : files.getSavesComponents()) {
            uploadManifest(manifest);
        }
        for(LauncherManifest manifest : files.getResourcepackComponents()) {
            uploadManifest(manifest);
        }
        for(LauncherManifest manifest : files.getOptionsComponents()) {
            uploadManifest(manifest);
        }
        for(Pair<LauncherManifest, LauncherModsDetails> manifest : files.getModsComponents()) {
            uploadManifest(manifest.getKey());
        }
    }

    @Override
    public void download() throws IOException {
        for(Pair<LauncherManifest, LauncherInstanceDetails> details : files.getInstanceComponents()) {
            if(!SyncUtil.isSyncing(details.getKey())) {
                continue;
            }
            InstanceData data;
            try {
                data = InstanceData.of(details, files);
            } catch (FileLoadException e) {
                throw new IOException(e);
            }
            new InstanceSynchronizer(data, files, callback).download();
        }
        for(LauncherManifest manifest : files.getSavesComponents()) {
            downloadManifest(manifest);
        }
        for(LauncherManifest manifest : files.getResourcepackComponents()) {
            downloadManifest(manifest);
        }
        for(LauncherManifest manifest : files.getOptionsComponents()) {
            downloadManifest(manifest);
        }
        for(Pair<LauncherManifest, LauncherModsDetails> manifest : files.getModsComponents()) {
            downloadManifest(manifest.getKey());
        }
    }

    private void uploadManifest(LauncherManifest manifest) throws IOException {
        if(!SyncUtil.isSyncing(manifest)) {
            return;
        }
        new ManifestSynchronizer(manifest, files, callback).upload();
    }

    private void downloadManifest(LauncherManifest manifest) throws IOException {
        if(!SyncUtil.isSyncing(manifest)) {
            return;
        }
        new ManifestSynchronizer(manifest, files, callback).download();
    }
}
