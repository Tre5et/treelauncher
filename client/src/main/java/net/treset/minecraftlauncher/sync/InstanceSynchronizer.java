package net.treset.minecraftlauncher.sync;

import net.treset.minecraftlauncher.data.InstanceData;

public class InstanceSynchronizer extends ManifestSynchronizer {
    private InstanceData instanceData;

    public InstanceSynchronizer(InstanceData instanceData, SyncCallback callback) {
        super(instanceData.getInstance().getKey(), callback);
        this.instanceData = instanceData;
    }

    @Override
    public void upload() {

    }

    @Override
    public void download() {

    }

    public InstanceData getInstanceData() {
        return instanceData;
    }

    public void setInstanceData(InstanceData instanceData) {
        this.instanceData = instanceData;
    }
}
