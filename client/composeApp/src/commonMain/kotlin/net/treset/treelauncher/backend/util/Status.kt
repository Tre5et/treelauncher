package net.treset.treelauncher.backend.util

import net.treset.treelauncher.localization.strings

class Status(
    private val stepProvider: StringProvider,
    private val detailsProvider: StringProvider,
    val progress: Float? = null
) {
    val step: String
        get() = stepProvider.get()

    val details: String
        get() = detailsProvider.get()
}

interface StringProvider {
    fun get(): String
}

open class FormatStringProvider(
    val get: () -> String
) : StringProvider {
    override fun get(): String = get()
}

class DetailsProvider(
    val message: () -> String,
    val index: Int,
    val total: Int
) : StringProvider {
    constructor(
        message: String,
        index: Int,
        total: Int
    ) : this({ message }, index, total)

    override fun get(): String = strings().statusDetailsMessage(message(), index, total)
}