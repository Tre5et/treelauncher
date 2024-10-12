package dev.treset.treelauncher.backend.update

import dev.treset.mcdl.json.GenericJsonParsable
import dev.treset.mcdl.json.SerializationException
import dev.treset.treelauncher.generic.PopupType
import dev.treset.treelauncher.localization.strings

class UpdaterStatus(
    var status: Status,
    var message: String? = null,
    var exceptions: List<String>? = null
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
