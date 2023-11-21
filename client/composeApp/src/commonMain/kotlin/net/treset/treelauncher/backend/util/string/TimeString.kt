package net.treset.treelauncher.backend.util.string

import net.treset.treelauncher.localization.strings

class TimeString(seconds: Long) : FormatString() {
    private var output: String

    init {
        output =
        if (seconds < 60) {
            "${seconds}${strings().units.seconds()}"
        } else if (seconds < 60 * 60) {
            "${seconds / 60}${strings().units.minutes()}"
        } else if (seconds < 60 * 60 * 10) {
            "${seconds / 3600}${strings().units.hours()} ${seconds % 3600 / 60}${strings().units.minutes()}"
        } else if (seconds < 60 * 60 * 24) {
            "${seconds / 3600}${strings().units.hours()}"
        } else if (seconds < 60 * 60 * 24 * 10) {
            "${seconds / (3600 * 24)}${strings().units.days()} ${seconds % (3600 * 24) / 3600}${strings().units.hours()}"
        } else {
            "${seconds / (3600 * 24)}${strings().units.days()}"
        }
    }

    override fun get(): String {
        return output
    }
}
