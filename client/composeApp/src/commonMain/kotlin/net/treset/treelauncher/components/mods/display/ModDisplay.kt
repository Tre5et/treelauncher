package net.treset.treelauncher.components.mods.display

abstract class ModDisplay {
    var onRecomposeData: (data: ModDisplayData) -> Unit = { _: ModDisplayData -> }
    abstract fun recomposeData(): ModDisplayData
}