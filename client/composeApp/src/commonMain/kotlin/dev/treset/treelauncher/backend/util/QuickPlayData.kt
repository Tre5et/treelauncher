package dev.treset.treelauncher.backend.util

class QuickPlayData(val type: Type, val name: String) {
    enum class Type {
        WORLD,
        SERVER,
        REALM
    }

}
