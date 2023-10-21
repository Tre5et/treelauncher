package net.treset.minecraftlauncher.sync;

import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.util.DownloadStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

public abstract class FileSynchronizer {
    public interface SyncCallback {

        void set(SyncStatus status);
    }
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

    protected static final Logger LOGGER = LogManager.getLogger(FileSynchronizer.class);

    protected SyncCallback callback;

    public FileSynchronizer(SyncCallback callback) {
        this.callback = callback;
    }

    protected void setStatus(SyncStatus status) {
        if(callback != null) {
            callback.set(new SyncStatus(status.getStep(), status.getStatus()));
        }
    }

    public SyncCallback getCallback() {
        return callback;
    }

    public void setCallback(SyncCallback callback) {
        this.callback = callback;
    }

    public abstract void upload() throws IOException;

    public abstract void download() throws IOException;

    public static String getStringFromType(LauncherManifestType type, Map<String, LauncherManifestType> typeConversion) {
        for (Map.Entry<String, LauncherManifestType> e: typeConversion.entrySet()) {
            if(e.getValue() == type) {
                return e.getKey();
            }
        }
        throw new IllegalStateException("Unable to find string for type " + type);
    }

    public static LauncherManifestType getChildType(LauncherManifestType type) {
        return switch (type) {
            case INSTANCES -> LauncherManifestType.INSTANCE_COMPONENT;
            case VERSIONS -> LauncherManifestType.VERSION_COMPONENT;
            case SAVES -> LauncherManifestType.SAVES_COMPONENT;
            case RESOURCEPACKS -> LauncherManifestType.RESOURCEPACKS_COMPONENT;
            case MODS -> LauncherManifestType.MODS_COMPONENT;
            case OPTIONS -> LauncherManifestType.OPTIONS_COMPONENT;
            default -> throw new IllegalStateException("Unable to find child type for type " + type);
        };
    }
}
