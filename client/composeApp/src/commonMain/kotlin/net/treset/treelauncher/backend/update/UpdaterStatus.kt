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
        val type: PopupElement.PopupType
    ) {
        UPDATING({ strings().updater.status.updatingTitle() }, { strings().updater.status.updatingMessage() }, PopupElement.PopupType.ERROR),
        SUCCESS({ strings().updater.status.successTitle() }, { strings().updater.status.successMessage() }, PopupElement.PopupType.SUCCESS),
        WARNING({ strings().updater.status.warningTitle() }, { strings().updater.status.warningMessage() }, PopupElement.PopupType.WARNING),
        FAILURE({ strings().updater.status.failureTitle() }, { strings().updater.status.failureMessage() }, PopupElement.PopupType.WARNING),
        FATAL({ strings().updater.status.fatalTitle() }, { strings().updater.status.fatalMessage() }, PopupElement.PopupType.ERROR);
    }

    companion object {
        @Throws(SerializationException::class)
        fun fromJson(json: String?): UpdaterStatus {
            return fromJson(json, UpdaterStatus::class.java)
        }
    }
}
