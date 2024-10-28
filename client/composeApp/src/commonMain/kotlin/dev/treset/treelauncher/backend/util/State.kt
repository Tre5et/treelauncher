package dev.treset.treelauncher.backend.util

import androidx.compose.runtime.MutableState

fun MutableState<Boolean>.toggle() {
    value = !value
}