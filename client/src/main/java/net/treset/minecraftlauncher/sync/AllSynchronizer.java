package net.treset.minecraftlauncher.sync;

import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.util.SyncUtil;
import net.treset.minecraftlauncher.util.exception.FileLoadException;

import java.io.IOException;
import java.util.ArrayList;

public class AllSynchronizer extends FileSynchronizer {
    private final LauncherFiles files;

    public AllSynchronizer(LauncherFiles files, SyncCallback callback) {
        super(callback);
        this.files = files;
    }

    @Override
    public void upload() throws IOException {
        synchronize(true);
    }

    @Override
    public void download() throws IOException {
        synchronize(false);
    }

    private void synchronize(boolean upload) throws IOException {
        ArrayList<IOException> exceptions = new ArrayList<>();
        files.getInstanceComponents().parallelStream().forEach((details) -> {
            if(!SyncUtil.isSyncing(details.getKey())) {
                return;
            }
            InstanceData data;
            try {
                data = InstanceData.of(details, files);
            } catch (FileLoadException e) {
                exceptions.add(new IOException(e));
                return;
            }
            InstanceSynchronizer synchronizer = new InstanceSynchronizer(data, files, callback);
            try {
                if(upload) {
                    synchronizer.upload();
                } else {
                    synchronizer.download();
                }
            } catch (IOException e) {
                exceptions.add(e);
            }
        });
        ArrayList<LauncherManifest> manifests = new ArrayList<>();
        manifests.addAll(files.getSavesComponents());
        manifests.addAll(files.getResourcepackComponents());
        manifests.addAll(files.getOptionsComponents());
        manifests.addAll(files.getModsComponents().stream().map(Pair::getKey).toList());
        manifests.parallelStream().forEach((manifest) -> {
            if(!SyncUtil.isSyncing(manifest)) {
                return;
            }
            ManifestSynchronizer synchronizer = new ManifestSynchronizer(manifest, files, callback);
            try {
                if(upload) {
                    synchronizer.upload();
                } else {
                    synchronizer.download();
                }
            } catch (IOException e) {
                exceptions.add(e);
            }
        });
        if(!exceptions.isEmpty()) {
            throw exceptions.get(0);
        }
    }
}
