package net.treset.treelauncher.components.mods.display

abstract class ModDisplay {
    var onRecomposeData: (data: ModDisplayData) -> Unit = { _: ModDisplayData -> }

    var onVisibility: (Boolean) -> Unit = {}
    var onDownloading: (Boolean) -> Unit = {}

    abstract fun recomposeData(): ModDisplayData
}