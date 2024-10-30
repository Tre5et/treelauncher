package dev.treset.treelauncher.backend.util

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap

typealias MutableStateList<T> = SnapshotStateList<T>

fun <T> List<T>.copyTo(other: MutableList<T>) {
    other.clear()
    other.addAll(this)
}

fun <T> MutableList<T>.assignFrom(other: List<T>) {
    other.copyTo(this)
}

typealias MutableStateMap<K, V> = SnapshotStateMap<K, V>

fun <K, V> Map<K, V>.copyTo(other: MutableMap<K, V>) {
    other.clear()
    other.putAll(this)
}

fun <K, V> MutableMap<K, V>.assignFrom(other: Map<K, V>) {
    other.copyTo(this)
}

fun MutableState<Boolean>.toggle() {
    value = !value
}
