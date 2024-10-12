package net.treset.treelauncher.backend.util

import dev.treset.mcdl.util.DownloadStatus
import net.treset.treelauncher.localization.strings

data class CreationStatus(val currentStep: DownloadStep, val downloadStatus: DownloadStatus?) {

    enum class DownloadStep(val message: () -> String) {
        STARTING({ strings().creator.status.starting() }),
        MODS({ strings().creator.status.mods() }),
        OPTIONS({ strings().creator.status.options() }),
        RESOURCEPACKS({ strings().creator.status.resourcepacks() }),
        SAVES({ strings().creator.status.saves() }),
        VERSION({ strings().creator.status.version.value() }),
        VERSION_VANILLA({ strings().creator.status.version.vanilla() }),
        VERSION_ASSETS({ strings().creator.status.version.assets() }),
        VERSION_LIBRARIES({ strings().creator.status.version.libraries() }),
        VERSION_FILE({ strings().creator.status.version.file() }),
        VERSION_FABRIC({ strings().creator.status.version.fabric() }),
        VERSION_FABRIC_LIBRARIES({ strings().creator.status.version.fabricLibraries() }),
        VERSION_FABRIC_FILE({ strings().creator.status.version.fabricFile() }),
        VERSION_FORGE({ strings().creator.status.version.forge() }),
        VERSION_FORGE_LIBRARIES({ strings().creator.status.version.forgeLibraries() }),
        VERSION_FORGE_FILE({ strings().creator.status.version.forgeFile() }),
        VERSION_QUILT({ strings().creator.status.version.quilt() }),
        VERSION_QUILT_LIBRARIES({ strings().creator.status.version.quiltLibraries() }),
        JAVA({ strings().creator.status.java() }),
        FINISHING({ strings().creator.status.finishing() });
    }
}
