package net.treset.treelauncher.backend.update

import net.treset.mc_version_loader.json.GenericJsonParsable
import net.treset.mc_version_loader.json.SerializationException
import net.treset.treelauncher.localization.strings

class UpdaterStatus(
    var status: Status,
    var message: String,
    var exceptions: List<String>
) : GenericJsonParsable() {
    enum class Status(
        val popupTitle: () -> String,
        val popupMessage: () -> String,
        val type: PopupType
    ) {
        UPDATING({ strings().updater.status.updatingTitle() }, { strings().updater.status.updatingMessage() }, PopupType.ERROR),
        SUCCESS({ strings().updater.status.successTitle() }, { strings().updater.status.successMessage() }, PopupType.SUCCESS),
        WARNING({ strings().updater.status.warningTitle() }, { strings().updater.status.warningMessage() }, PopupType.WARNING),
        FAILURE({ strings().updater.status.failureTitle() }, { strings().updater.status.failureMessage() }, PopupType.WARNING),
        FATAL({ strings().updater.status.fatalTitle() }, { strings().updater.status.fatalMessage() }, PopupType.ERROR);
    }

    companion object {
        @Throws(SerializationException::class)
        fun fromJson(json: String?): UpdaterStatus {
            return fromJson(json, UpdaterStatus::class.java)
        }
    }
}

enum class PopupType {
    ERROR,
    WARNING,
    SUCCESS
}
