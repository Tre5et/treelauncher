package dev.treset.treelauncher.backend.launching

import io.github.oshai.kotlinlogging.KotlinLogging
import dev.treset.mcdl.auth.data.UserData
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.data.LauncherLaunchArgument
import dev.treset.treelauncher.backend.data.manifest.InstanceComponent
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
    private var instance: InstanceComponent,
    var offline: Boolean,
    var minecraftUser: UserData?,
    private var quickPlayData: QuickPlayData?
) {
    @Throws(GameCommandException::class)
    fun makeStartCommand() {
        if (instance.versionComponents.value.isEmpty()) {
            throw GameCommandException("Unable to create start command: no version available")
        }
        var assetsIndex: String? = null
        for (v in instance.versionComponents.value) {
            if (!v.assets.value.isNullOrBlank()) {
                assetsIndex = v.assets.value
                break
            }
        }
        if (assetsIndex == null) {
            throw GameCommandException("Unable to create start command: unable to determine asset index")
        }

        var mainClass: String? = null
        for (v in instance.versionComponents.value) {
            if (v.mainClass.value.isNotBlank()) {
                mainClass = v.mainClass.value
                break
            }
        }
        if (mainClass == null) {
            throw GameCommandException("Unable to create start command: unable to determine main class")
        }

        val libraries: MutableList<String> = mutableListOf()
        for (v in instance.versionComponents.value) {
            for (l in v.libraries) {
                val library = LauncherFile.of(instance.librariesDir, l).absolutePath
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

        for (v in instance.versionComponents.value) {
            if (!v.mainFile.value.isNullOrBlank()) {
                libraries.add(LauncherFile.of(v.directory, v.mainFile.value!!).absolutePath)
            }
        }

        val natives: MutableList<String> = mutableListOf()
        for(v in instance.versionComponents.value) {
            if(!v.natives.value.isNullOrBlank()) {
                natives.add(LauncherFile.of(v.directory, v.natives.value!!).absolutePath)
            }
        }
        if(natives.isEmpty()) {
            natives.add(LauncherFile.of(instance.javaComponent.value.directory, "libs").absolutePath)
        }
        val nativesDir = natives.joinToString(File.pathSeparator)

        val gameDir: LauncherFile = instance.gameDataDir
        if (!gameDir.isDirectory()) {
            throw GameCommandException("Unable to create start command: game directory is not a directory: directory=${gameDir.absolutePath}")
        }

        processBuilder.directory(gameDir)
        processBuilder.command(mutableListOf())
        processBuilder.command().add(LauncherFile.of(instance.javaComponent.value.directory, "bin", "javaw").path)

        val jvmArgs = mutableListOf<LauncherLaunchArgument>()
        for(v in instance.versionComponents.value.reversed()) {
            jvmArgs.addAll(v.jvmArguments)
        }
        jvmArgs.addAll(instance.jvmArguments.toMutableList())

        val gameArgs = mutableListOf<LauncherLaunchArgument>()
        for(v in instance.versionComponents.value.reversed()) {
            gameArgs.addAll(v.gameArguments)
        }

        val args = jvmArgs + LauncherLaunchArgument(mainClass) + gameArgs

        val context = CommandContext(
            offline,
            minecraftUser,
            instance.gameDataDir.absolutePath,
            instance.resourcepacksComponent.value.directory.absolutePath,
            instance.assetsDir.absolutePath,
            assetsIndex,
            libraries,
            instance.librariesDir.absolutePath,
            nativesDir,
            Strings.game.versionName(instance),
            Strings.game.versionType(instance),
            instance.resX.value?.toString(),
            instance.resY.value?.toString(),
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
        LOGGER.info { "Created start command, instance=${instance.id}" }
    }

    private fun constructFeatures(): List<String> {
        val features: MutableList<String> = mutableListOf()
        features.addAll(instance.features)
        quickPlayData?.let {
            when (it.type) {
                QuickPlayData.Type.WORLD -> features.add("is_quick_play_singleplayer")
                QuickPlayData.Type.SERVER -> features.add("is_quick_play_multiplayer")
                QuickPlayData.Type.REALM -> features.add("is_quick_play_realms")
            }
        }
        if(instance.resX.value != null && instance.resY.value != null) {
            features.add("has_custom_resolution")
        }
        return features
    }

    @Throws(GameCommandException::class)
    private fun appendArguments(
        pb: ProcessBuilder,
        args: List<LauncherLaunchArgument>,
        context: CommandContext
    ) {
        val features = constructFeatures()
        val exceptionQueue: MutableList<GameCommandException> = mutableListOf()
        for (a in args) {
            try {
                appendArgument(
                    pb,
                    a,
                    context,
                    features
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

    private var lastCheckedArgument: String? = null
    @Throws(GameCommandException::class)
    private fun appendArgument(
        pb: ProcessBuilder,
        a: LauncherLaunchArgument,
        context: CommandContext,
        features: List<String>
    ) {
        if (a.isActive(features)) {
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
            LOGGER.debug { "Argument: ${if(lastCheckedArgument?.contains("accessToken") == true) "**********" else a.parsedArgument}" }
            lastCheckedArgument = a.parsedArgument
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
