package dev.treset.treelauncher.backend.util

fun <T> List<T>.copyTo(other: MutableList<T>) {
    other.clear()
    other.addAll(this)
}

fun <T> MutableList<T>.assignFrom(other: List<T>) {
    other.copyTo(this)
}