package net.treset.treelauncher.backend.util

import net.treset.mc_version_loader.util.DownloadStatus
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
        VERSION_FABRIC({ strings().creator.status.version.fabric() }),
        JAVA({ strings().creator.status.java() }),
        FINISHING({ strings().creator.status.finishing() });
    }
}
