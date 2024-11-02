package dev.treset.treelauncher.backend.launching

import io.github.oshai.kotlinlogging.KotlinLogging
import dev.treset.mcdl.auth.data.UserData
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.data.InstanceData
import dev.treset.treelauncher.backend.data.LauncherLaunchArgument
import dev.treset.treelauncher.backend.util.QuickPlayData
import dev.treset.treelauncher.backend.util.exception.GameCommandException
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.libraryContained
import dev.treset.treelauncher.localization.Strings
import java.io.File

class CommandContext(
    val offline: Boolean,
    val minecraftUser: UserData?,
    val gameDataDir: String,
    val resourcepacksDir: String,
    val assetsDir: String,
    val assetsIndex: String,
    val libraries: List<String>,
    val librariesDir: String,
    val nativesDir: String,
    val versionName: String,
    val versionType: String,
    val resX: String?,
    val resY: String?,
    val quickPlayData: QuickPlayData?
)

class CommandBuilder(
    var processBuilder: ProcessBuilder,
    private var instanceData: InstanceData,
    var offline: Boolean,
    var minecraftUser: UserData?,
    private var quickPlayData: QuickPlayData?
) {
    @Throws(GameCommandException::class)
    fun makeStartCommand() {
        if (instanceData.versionComponents.isEmpty()) {
            throw GameCommandException("Unable to create start command: no version available")
        }
        var assetsIndex: String? = null
        for (v in instanceData.versionComponents) {
            if (!v.assets.value.isNullOrBlank()) {
                assetsIndex = v.assets.value
                break
            }
        }
        if (assetsIndex == null) {
            throw GameCommandException("Unable to create start command: unable to determine asset index")
        }

        var mainClass: String? = null
        for (v in instanceData.versionComponents) {
            if (v.mainClass.value.isNotBlank()) {
                mainClass = v.mainClass.value
                break
            }
        }
        if (mainClass == null) {
            throw GameCommandException("Unable to create start command: unable to determine main class")
        }

        val libraries: MutableList<String> = mutableListOf()
        for (v in instanceData.versionComponents) {
            for (l in v.libraries) {
                val library = LauncherFile.of(instanceData.librariesDir, l).absolutePath
                if(l.libraryContained(libraries)) {
                    LOGGER.debug { "Skipping declared library $l because another version is already present" }
                    continue
                }
                libraries.add(library)
            }
        }
        if (libraries.isEmpty()) {
            throw GameCommandException("Unable to create start command: unable to determine libraries")
        }

        for (v in instanceData.versionComponents) {
            if (!v.mainFile.value.isNullOrBlank()) {
                libraries.add(LauncherFile.of(v.directory, v.mainFile.value!!).absolutePath)
            }
        }

        val natives: MutableList<String> = mutableListOf()
        for(v in instanceData.versionComponents) {
            if(!v.natives.value.isNullOrBlank()) {
                natives.add(LauncherFile.of(v.directory, v.natives.value!!).absolutePath)
            }
        }
        if(natives.isEmpty()) {
            natives.add(LauncherFile.of(instanceData.javaComponent.value.directory, "libs").absolutePath)
        }
        val nativesDir = natives.joinToString(File.pathSeparator)

        var resX: String? = null
        var resY: String? = null
        for (f in instanceData.instance.features) {
            if (f.feature == "resolution_x") {
                resX = f.value
            }
            if (f.feature == "resolution_y") {
                resY = f.value
            }
        }
        val gameDir: LauncherFile = instanceData.gameDataDir
        if (!gameDir.isDirectory()) {
            throw GameCommandException("Unable to create start command: game directory is not a directory: directory=${gameDir.absolutePath}")
        }

        processBuilder.directory(gameDir)
        processBuilder.command(mutableListOf())
        processBuilder.command().add(LauncherFile.of(instanceData.javaComponent.value.directory, "bin", "java").path)

        val jvmArgs = instanceData.instance.jvmArguments.toMutableList()
        for(v in instanceData.versionComponents) {
            v.jvmArguments.forEach {
                if(!jvmArgs.contains(it)) {
                    jvmArgs.add(it)
                }
            }
        }

        val gameArgs = mutableListOf<LauncherLaunchArgument>()
        for(v in instanceData.versionComponents) {
            v.gameArguments.forEach {
                if(!gameArgs.contains(it)) {
                    gameArgs.add(it)
                }
            }
        }

        val args = jvmArgs + LauncherLaunchArgument(mainClass) + gameArgs

        val context = CommandContext(
            offline,
            minecraftUser,
            instanceData.gameDataDir.absolutePath,
            instanceData.resourcepacksComponent.value.directory.absolutePath,
            instanceData.assetsDir.absolutePath,
            assetsIndex,
            libraries,
            instanceData.librariesDir.absolutePath,
            nativesDir,
            Strings.game.versionName(instanceData),
            Strings.game.versionType(instanceData),
            resX,
            resY,
            quickPlayData
        )

        try {
            appendArguments(
                processBuilder,
                args,
                context
            )
        } catch (e: GameCommandException) {
            throw GameCommandException("Unable to create start command: unable to append arguments", e)
        }
        LOGGER.info { "Created start command, instance=${instanceData.instance.id}" }
    }

    @Throws(GameCommandException::class)
    private fun appendArguments(
        pb: ProcessBuilder,
        args: List<LauncherLaunchArgument>,
        context: CommandContext
    ) {
        val exceptionQueue: MutableList<GameCommandException> = mutableListOf()
        for (a in args) {
            try {
                appendArgument(
                    pb,
                    a,
                    context
                )
            } catch (e: GameCommandException) {
                exceptionQueue.add(e)
                LOGGER.warn { "Unable to append arguments: unable to append argument: argument=$a" }
            }
        }
        if (exceptionQueue.isNotEmpty()) {
            throw GameCommandException("Unable to append ${exceptionQueue.size} arguments", exceptionQueue[0])
        }
    }

    @Throws(GameCommandException::class)
    private fun appendArgument(
        pb: ProcessBuilder,
        a: LauncherLaunchArgument,
        context: CommandContext
    ) {
        if (a.isActive(instanceData.instance.features)) {
            val replacements: MutableMap<String, String> = mutableMapOf()
            val exceptionQueue: MutableList<GameCommandException> = mutableListOf()
            for (r in a.replacementValues) {
                try {
                    val replacement = getReplacement(
                        r,
                        context
                    )
                    replacements[r] = replacement
                } catch (e: GameCommandException) {
                    exceptionQueue.add(e)
                    LOGGER.warn(e) { "Unable to append argument: unable to replace variable: argument=${a.argument}, variable=${r}" }
                    replacements[r] = ""
                }
            }
            if (exceptionQueue.isNotEmpty()) {
                LOGGER.error(exceptionQueue[0]) { "Unable to append argument: unable to replace ${exceptionQueue.size} variables: argument=${a.argument}" }
            }
            a.replace(replacements)
            if (!a.isFinished) {
                throw GameCommandException("Unable to append argument: unable to replace all variables: argument=${a.argument}")
            }
            pb.command().add(a.parsedArgument)
        }
    }

    @Throws(GameCommandException::class)
    private fun getReplacement(
        key: String,
        context: CommandContext
    ): String {
        return when (key) {
            "natives_directory" -> {
                context.nativesDir
            }

            "launcher_name" -> {
                Strings.launcher.name()
            }

            "launcher_version" -> {
                Strings.launcher.version()
            }

            "classpath" -> {
                val sb = StringBuilder()
                for (l in context.libraries) {
                    sb.append(l).append(";")
                }
                sb.substring(0, sb.length - 1)
            }

            "classpath_separator" -> {
                File.pathSeparator
            }

            "library_directory" -> {
                context.librariesDir
            }

            "auth_player_name" -> {
                if(context.minecraftUser != null && !context.offline) {
                    context.minecraftUser.username
                } else {
                    "LocalPlayer"
                }
            }

            "version_name" -> {
                context.versionName
            }

            "game_directory" -> {
                context.gameDataDir
            }

            "assets_root" -> {
                context.assetsDir
            }

            "game_assets" -> {
                context.assetsDir
            }

            "assets_index_name" -> {
                context.assetsIndex
            }

            "auth_uuid" -> {
                if(context.minecraftUser != null && !context.offline) {
                    context.minecraftUser.uuid
                } else {
                    "00000000000000000000000000000000"
                }
            }

            "auth_xuid" -> {
                if(context.minecraftUser != null && !context.offline) {
                    context.minecraftUser.xuid
                } else {
                    ""
                }
            }

            "auth_access_token" -> {
                if(context.minecraftUser != null && !context.offline) {
                    context.minecraftUser.accessToken
                } else {
                    ""
                }
            }

            "clientid" -> {
                AppSettings.clientId.value
            }

            "user_type" -> {
                if(context.minecraftUser != null && !context.offline) {
                    "msa"
                } else {
                    ""
                }
            }

            "auth_session" -> {
                if(context.minecraftUser != null && !context.offline) {
                    "token:${context.minecraftUser.accessToken}:${context.minecraftUser.uuid}"
                } else {
                    "token::00000000000000000000000000000000"
                }
            }

            "version_type" -> {
                context.versionType
            }

            "resolution_width" -> {
                context.resX ?: ""
            }

            "resolution_height" -> {
                context.resY ?: ""
            }

            "quickPlayPath" -> {
                "quickPlay/log.json"
            }

            "quickPlaySingleplayer" -> {
                if (context.quickPlayData?.type == QuickPlayData.Type.WORLD) context.quickPlayData.name else ""
            }

            "quickPlayMultiplayer" -> {
                if (context.quickPlayData?.type == QuickPlayData.Type.SERVER) context.quickPlayData.name else ""
            }

            "quickPlayRealms" -> {
                if (context.quickPlayData?.type === QuickPlayData.Type.REALM) context.quickPlayData.name else ""
            }

            else -> throw GameCommandException("Unknown environment variable: key=$key")
        }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
