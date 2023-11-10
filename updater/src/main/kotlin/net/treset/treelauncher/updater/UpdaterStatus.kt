package net.treset.treelauncher.updater

import kotlinx.serialization.Serializable

@Serializable
data class UpdaterStatus(
    val status: Status,
    val message: String? = null,
    val exceptions: List<String>? = null
)

@Serializable
enum class Status {
    UPDATING,
    SUCCESS,
    WARNING,
    FAILURE,
    FATAL
}