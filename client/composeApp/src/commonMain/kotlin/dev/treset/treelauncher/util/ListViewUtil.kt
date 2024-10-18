package dev.treset.treelauncher.util

import dev.treset.treelauncher.localization.Strings

enum class DetailsListDisplay(
    val displayName: () -> String
) {
    FULL({ Strings.list.full() }),
    COMPACT({ Strings.list.compact() }),
    MINIMAL({ Strings.list.minimal() });

    override fun toString(): String = displayName()
}