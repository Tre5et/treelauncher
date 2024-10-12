package net.treset.treelauncher.backend.util

import dev.treset.mcdl.util.DownloadStatus
import net.treset.treelauncher.localization.strings

open class Status(
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
    override fun get(): String = get.invoke()
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

class StatusProvider(
    val step: StringProvider?,
    var total: Int,
    val onStatus: (Status) -> Unit,
    val parent: StatusProvider? = null
) {
    var index = 1
    var lastStatus: Status? = null
    var finished = false

    fun next(
        message: () -> String
    ) {
        step?.let {
            lastStatus = Status(
                step,
                DetailsProvider(message, index++, if (total >= index) total else index),
                index.toFloat() / total.toFloat()
            ).also(onStatus)
        }
    }

    fun next(
        message: String
    ) = next { message }

    fun next() = next("")

    fun download(status: DownloadStatus, before: Int, after: Int) {
        step?.let {
            index = status.currentAmount + before
            total = status.totalAmount + before + after
            lastStatus = Status(
                step,
                DetailsProvider(status.currentFile, index, total),
                index.toFloat() / total.toFloat()
            ).also(onStatus)
        }
    }

    fun finish(
        message: () -> String
    ) {
        step?.let {
            finished = true
            lastStatus = Status(
                step,
                object : StringProvider { override fun get(): String = message() },
                1f
            ).also(onStatus)
            parent?.resend()
        }
    }

    fun finish(
        message: String
    ) = finish { message }

    fun finish() = finish("")

    fun subStep(
        step: StringProvider,
        total: Int
    ) = StatusProvider(step, total, onStatus, this)

    private fun resend() {
        lastStatus?.let(onStatus)
        if(finished) {
            parent?.resend()
        }
    }
}