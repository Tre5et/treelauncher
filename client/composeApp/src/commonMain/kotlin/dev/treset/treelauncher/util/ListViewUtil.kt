package dev.treset.treelauncher.util

import dev.treset.treelauncher.localization.strings

enum class DetailsListDisplay(
    val displayName: () -> String
) {
    FULL({ strings().list.full() }),
    COMPACT({ strings().list.compact() }),
    MINIMAL({ strings().list.minimal() });

    override fun toString(): String = displayName()
}