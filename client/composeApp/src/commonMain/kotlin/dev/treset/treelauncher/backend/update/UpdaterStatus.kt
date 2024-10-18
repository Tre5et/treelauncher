package dev.treset.treelauncher.backend.update

import dev.treset.mcdl.json.GenericJsonParsable
import dev.treset.mcdl.json.SerializationException
import dev.treset.treelauncher.generic.PopupType
import dev.treset.treelauncher.localization.Strings

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
        UPDATING({ Strings.updater.status.updatingTitle() }, { Strings.updater.status.updatingMessage() }, PopupType.ERROR),
        SUCCESS({ Strings.updater.status.successTitle() }, { Strings.updater.status.successMessage() }, PopupType.SUCCESS),
        WARNING({ Strings.updater.status.warningTitle() }, { Strings.updater.status.warningMessage() }, PopupType.WARNING),
        FAILURE({ Strings.updater.status.failureTitle() }, { Strings.updater.status.failureMessage() }, PopupType.WARNING),
        FATAL({ Strings.updater.status.fatalTitle() }, { Strings.updater.status.fatalMessage() }, PopupType.ERROR);
    }

    companion object {
        @Throws(SerializationException::class)
        fun fromJson(json: String?): UpdaterStatus {
            return fromJson(json, UpdaterStatus::class.java)
        }
    }
}
