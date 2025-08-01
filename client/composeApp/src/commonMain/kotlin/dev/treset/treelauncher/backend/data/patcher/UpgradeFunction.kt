package dev.treset.treelauncher.backend.data.patcher

import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.util.StatusProvider
import dev.treset.treelauncher.backend.util.Version
import dev.treset.treelauncher.backend.util.file.AnyCheckFunction
import dev.treset.treelauncher.backend.util.file.FileChecker
import dev.treset.treelauncher.backend.util.file.SimpleDirChecker
import dev.treset.treelauncher.backend.util.file.SimpleFileChecker
import dev.treset.treelauncher.backend.util.file.StringCheckFunction


val FileChecker.Companion.ALL: FileChecker
    get() = SimpleFileChecker()
val FileChecker.Companion.LAUNCHER_MANIFEST: FileChecker
    get() = SimpleFileChecker(StringCheckFunction(appConfig().manifestFileName))
val FileChecker.Companion.META: FileChecker
    get() = SimpleDirChecker(StringCheckFunction(".launcher"))
val FileChecker.Companion.ASSETS: FileChecker
    get() = SimpleDirChecker(StringCheckFunction("assets"))
val FileChecker.Companion.GAME_DATA: FileChecker
    get() = SimpleDirChecker(StringCheckFunction("game_data"))
val FileChecker.Companion.LIBRARIES: FileChecker
    get() = SimpleDirChecker(StringCheckFunction("libraries"))
val FileChecker.Companion.INSTANCES_PARENT: FileChecker
    get() = SimpleFileChecker(StringCheckFunction("instances"), StringCheckFunction(appConfig().manifestFileName))
val FileChecker.Companion.INSTANCE_MANIFESTS: FileChecker
    get() = SimpleFileChecker(StringCheckFunction("instances"), AnyCheckFunction(), StringCheckFunction(appConfig().manifestFileName))
val FileChecker.Companion.INSTANCE_FILES: FileChecker
    get() = SimpleDirChecker(StringCheckFunction("instances"), AnyCheckFunction())
val FileChecker.Companion.JAVA_PARENT: FileChecker
    get() = SimpleFileChecker(StringCheckFunction("java_components"), StringCheckFunction(appConfig().manifestFileName))
val FileChecker.Companion.JAVA_MANIFESTS: FileChecker
    get() = SimpleFileChecker(StringCheckFunction("java_components"), AnyCheckFunction(), StringCheckFunction(appConfig().manifestFileName))
val FileChecker.Companion.JAVA_FILES: FileChecker
    get() = SimpleDirChecker(StringCheckFunction("java_components"), AnyCheckFunction())
val FileChecker.Companion.VERSION_PARENT: FileChecker
    get() = SimpleFileChecker(StringCheckFunction("version_components"), StringCheckFunction(appConfig().manifestFileName))
val FileChecker.Companion.VERSION_MANIFESTS: FileChecker
    get() = SimpleFileChecker(StringCheckFunction("version_components"), AnyCheckFunction(), StringCheckFunction(appConfig().manifestFileName))
val FileChecker.Companion.VERSION_FILES: FileChecker
    get() = SimpleDirChecker(StringCheckFunction("version_components"), AnyCheckFunction())
val FileChecker.Companion.SAVES_PARENT: FileChecker
    get() = SimpleFileChecker(StringCheckFunction("saves_components"), StringCheckFunction(appConfig().manifestFileName))
val FileChecker.Companion.SAVES_MANIFESTS: FileChecker
    get() = SimpleFileChecker(StringCheckFunction("saves_components"), AnyCheckFunction(), StringCheckFunction(appConfig().manifestFileName))
val FileChecker.Companion.SAVES_FILES: FileChecker
    get() = SimpleDirChecker(StringCheckFunction("saves_components"), AnyCheckFunction())
val FileChecker.Companion.RESOURCEPACKS_PARENT: FileChecker
    get() = SimpleFileChecker(StringCheckFunction("resourcepack_components"), StringCheckFunction(appConfig().manifestFileName))
val FileChecker.Companion.RESOURCEPACKS_MANIFESTS: FileChecker
    get() = SimpleFileChecker(StringCheckFunction("resourcepack_components"), AnyCheckFunction(), StringCheckFunction(appConfig().manifestFileName))
val FileChecker.Companion.RESOURCEPACKS_FILES: FileChecker
    get() = SimpleDirChecker(StringCheckFunction("resourcepack_components"), AnyCheckFunction())
val FileChecker.Companion.OPTIONS_PARENT: FileChecker
    get() = SimpleFileChecker(StringCheckFunction("option_components"), StringCheckFunction(appConfig().manifestFileName))
val FileChecker.Companion.OPTIONS_MANIFESTS: SimpleFileChecker
    get() = SimpleFileChecker(StringCheckFunction("option_components"), AnyCheckFunction(), StringCheckFunction(appConfig().manifestFileName))
val FileChecker.Companion.OPTIONS_FILES: FileChecker
    get() = SimpleDirChecker(StringCheckFunction("option_components"), AnyCheckFunction())
val FileChecker.Companion.MODS_PARENT: FileChecker
    get() = SimpleFileChecker(StringCheckFunction("mods_components"), StringCheckFunction(appConfig().manifestFileName))
val FileChecker.Companion.MODS_MANIFESTS: FileChecker
    get() = SimpleFileChecker(StringCheckFunction("mods_components"), AnyCheckFunction(), StringCheckFunction(appConfig().manifestFileName))
val FileChecker.Companion.MODS_FILES: FileChecker
    get() = SimpleDirChecker(StringCheckFunction("mods_components"), AnyCheckFunction())

class UpgradeFunction(
    val function: (StatusProvider) -> Unit,
    val applies: () -> Boolean,
    val fileCheckers: List<FileChecker> = emptyList()
) {
    constructor(
        function: (StatusProvider) -> Unit,
        version: Version,
        vararg fileChecker: FileChecker = arrayOf(FileChecker.ALL)
    ): this(
        function,
        { appConfig().dataVersion >= version && Version.fromString(AppSettings.dataVersion.value) < version },
        fileChecker.toList()
    )

    constructor(
        function: (StatusProvider) -> Unit,
        vararg fileChecker: FileChecker = arrayOf(FileChecker.ALL)
    ): this(
        function,
        { appConfig().dataVersion > Version.fromString(AppSettings.dataVersion.value)},
        fileChecker.toList()
    )

    fun execute(statusProvider: StatusProvider) {
        if(applies()) {
            function(statusProvider)
        }
    }
}