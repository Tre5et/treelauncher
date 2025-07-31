package dev.treset.treelauncher.backend.util

import dev.treset.mcdl.util.DownloadStatus
import dev.treset.treelauncher.localization.Strings

typealias StatusReceiver = (List<Status>) -> Unit

open class Status(
    private val stepProvider: StringProvider,
    private val detailsProvider: StringProvider? = null,
    val progress: Float? = null
) {
    val step: String
        get() = stepProvider.get()

    val details: String
        get() = detailsProvider?.get() ?: ""
}

interface StringProvider {
    fun get(): String
}

class SimpleStringProvider(
    private val string: String
) : StringProvider {
    override fun get(): String = string
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

    override fun get(): String = Strings.statusDetailsMessage(message(), index, total)
}

class StatusProvider(
    val step: StringProvider?,
    var total: Int,
    val onStatus: StatusReceiver,
    val parent: StatusProvider? = null
) {
    var index = 1
    var lastStatus: Status? = null
    var finished = false

    val status: Status?
        get() = lastStatus

    val statusList: MutableList<Status>
        get() = parent?.let {
                it.statusList.also { l -> status?.let { s -> l.add(s) } }
            } ?: status?.let { mutableListOf(it) } ?: mutableListOf()

    fun custom(
        current: Int,
        total: Int,
        message: () -> String
    ) {
        step?.let {
            lastStatus = Status(
                step,
                DetailsProvider(message, current, total),
                ((index.toFloat()) / (total.toFloat() + 1f))
            )
        }
        onStatus(statusList)
    }

    fun custom(
        current: Int,
        total: Int,
        message: String
    ) = custom(current, total, { message })

    fun unknown(
        message: () -> String,
    ) {
        step?.let {
            lastStatus = Status(
                step,
                DetailsProvider(message, -1, -1),
                -1f
            )
        }
        onStatus(statusList)
    }

    fun unknown(
        message: String,
    ) = unknown({ message })

    fun next(
        message: () -> String
    ) {
        step?.let {
            lastStatus = Status(
                step,
                DetailsProvider(message, index, if (total >= index) total else index),
                if(total < 0) -1f else ((index.toFloat()) / (total.toFloat() + 1f))
            )
            onStatus(statusList)
            index++
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
                if(total < 0) -1f else ((index.toFloat()) / (total.toFloat() + 1f))
            )
            onStatus(statusList)
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
            )
            onStatus(statusList)
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
        onStatus(statusList)
        if(finished) {
            parent?.resend()
        }
    }
}