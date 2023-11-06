data class UpdaterStatus(
    val status: Status,
    val message: String? = null,
    val exceptions: List<Exception>? = null
)

enum class Status {
    UPDATING,
    SUCCESS,
    WARNING,
    ERROR
}