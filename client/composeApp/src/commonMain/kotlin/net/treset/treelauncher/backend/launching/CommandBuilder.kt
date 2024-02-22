package net.treset.treelauncher.backend.launching

import io.github.oshai.kotlinlogging.KotlinLogging
import net.hycrafthd.minecraft_authenticator.login.User
import net.treset.mc_version_loader.launcher.LauncherLaunchArgument
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.backend.util.QuickPlayData
import net.treset.treelauncher.backend.util.exception.GameCommandException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.localization.strings

class CommandBuilder(
    var processBuilder: ProcessBuilder,
    private var instanceData: InstanceData,
    var minecraftUser: User,
    private var quickPlayData: QuickPlayData?
) {
    @Throws(GameCommandException::class)
    fun makeStartCommand() {
        if (instanceData.versionComponents.isEmpty()) {
            throw GameCommandException("Unable to create start command: no version available")
        }
        var assetsIndex: String? = null
        for (v in instanceData.versionComponents) {
            if (v.second.assets != null && v.second.assets.isNotBlank()) {
                assetsIndex = v.second.assets
                break
            }
        }
        if (assetsIndex == null) {
            throw GameCommandException("Unable to create start command: unable to determine asset index")
        }

        var mainClass: String? = null
        for (v in instanceData.versionComponents) {
            if (v.second.mainClass != null && v.second.mainClass.isNotBlank()) {
                mainClass = v.second.mainClass
                break
            }
        }
        if (mainClass == null) {
            throw GameCommandException("Unable to create start command: unable to determine main class")
        }

        val libraries: MutableList<String> = mutableListOf()
        for (v in instanceData.versionComponents) {
            for (l in v.second.libraries) {
                val library = LauncherFile.of(instanceData.librariesDir, l).absolutePath
                if(libraries.any { it == library }) continue
                libraries.add(library)
            }
        }
        if (libraries.isEmpty()) {
            throw GameCommandException("Unable to create start command: unable to determine libraries")
        }

        for (v in instanceData.versionComponents) {
            if (v.second.mainFile != null && v.second.mainFile.isNotBlank()) {
                libraries.add(LauncherFile.of(v.first.directory, v.second.mainFile).absolutePath)
            }
        }

        var resX: String? = null
        var resY: String? = null
        for (f in instanceData.instance.second.features) {
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
        processBuilder.command()
            .add(LauncherFile.of(instanceData.javaComponent.directory, "bin", "java").path)

        val argOrder: MutableList<Array<LauncherLaunchArgument>> = mutableListOf(instanceData.instance.second.jvmArguments.toTypedArray())
        for (v in instanceData.versionComponents) {
            argOrder.add(v.second.jvmArguments.toTypedArray())
        }
        argOrder.add(arrayOf(
            LauncherLaunchArgument(
                mainClass,
                null,
                null,
                null,
                null
            )
        ))
        for (v in instanceData.versionComponents) {
            argOrder.add(v.second.gameArguments.toTypedArray())
        }
        try {
            appendArguments(
                processBuilder,
                argOrder.toTypedArray(),
                instanceData,
                minecraftUser,
                instanceData.gameDataDir.absolutePath,
                instanceData.assetsDir.absolutePath,
                assetsIndex,
                libraries,
                resX,
                resY,
                quickPlayData
            )
        } catch (e: GameCommandException) {
            throw GameCommandException("Unable to create start command: unable to append arguments", e)
        }
        LOGGER.info { "Created start command, instance=${instanceData.instance.first.id}" }
    }

    private fun appendArguments(
        pb: ProcessBuilder,
        argOrder: Array<Array<LauncherLaunchArgument>>,
        instanceData: InstanceData,
        minecraftUser: User,
        gameDataDir: String,
        assetsDir: String,
        assetsIndex: String,
        libraries: List<String>,
        resX: String?,
        resY: String?,
        quickPlayData: QuickPlayData?
    ) {
        for(args in argOrder) {
            appendArguments(
                pb,
                args,
                instanceData,
                minecraftUser,
                gameDataDir,
                assetsDir,
                assetsIndex,
                libraries,
                resX,
                resY,
                quickPlayData
            )
        }
    }

    @Throws(GameCommandException::class)
    private fun appendArguments(
        pb: ProcessBuilder,
        args: Array<LauncherLaunchArgument>,
        instanceData: InstanceData,
        minecraftUser: User,
        gameDataDir: String,
        assetsDir: String,
        assetsIndex: String,
        libraries: List<String>,
        resX: String?,
        resY: String?,
        quickPlayData: QuickPlayData?
    ) {
        val exceptionQueue: MutableList<GameCommandException> = mutableListOf()
        for (a in args) {
            try {
                appendArgument(
                    pb,
                    instanceData,
                    minecraftUser,
                    gameDataDir,
                    assetsDir,
                    assetsIndex,
                    libraries,
                    resX,
                    resY,
                    a,
                    quickPlayData
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
        instanceData: InstanceData,
        minecraftUser: User,
        gameDataDir: String,
        assetsDir: String,
        assetsIndex: String,
        libraries: List<String>,
        resX: String?,
        resY: String?,
        a: LauncherLaunchArgument,
        quickPlayData: QuickPlayData?
    ) {
        if (a.isActive(instanceData.instance.second.features)) {
            val replacements: MutableMap<String, String> = mutableMapOf()
            val exceptionQueue: MutableList<GameCommandException> = mutableListOf()
            for (r in a.replacementValues) {
                try {
                    val replacement = getReplacement(
                        r,
                        gameDataDir,
                        instanceData.javaComponent.directory,
                        assetsDir,
                        instanceData.resourcepacksComponent.directory,
                        assetsIndex,
                        libraries,
                        minecraftUser,
                        strings().game.versionName(instanceData),
                        strings().game.versionType(instanceData),
                        resX,
                        resY,
                        quickPlayData
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
                throw GameCommandException("Unable to append argument: unable to replace all variables: argument=$a")
            }
            pb.command().add(a.parsedArgument)
        }
    }

    @Throws(GameCommandException::class)
    private fun getReplacement(
        key: String,
        gameDir: String,
        javaDir: String,
        assetsDir: String,
        resourcepackDir: String,
        assetsIndex: String,
        libraries: List<String>,
        minecraftUser: User,
        versionName: String,
        versionType: String,
        resX: String?,
        resY: String?,
        quickPlayData: QuickPlayData?
    ): String {
        return when (key) {
            "natives_directory" -> {
                javaDir + "lib"
            }

            "launcher_name" -> {
                strings().launcher.name()
            }

            "launcher_version" -> {
                strings().launcher.version()
            }

            "classpath" -> {
                val sb = StringBuilder()
                for (l in libraries) {
                    sb.append(l).append(";")
                }
                sb.substring(0, sb.length - 1)
            }

            "auth_player_name" -> {
                minecraftUser.name()
            }

            "version_name" -> {
                versionName
            }

            "game_directory" -> {
                gameDir
            }

            "resourcepack_directory" -> {
                resourcepackDir
            }

            "assets_root" -> {
                assetsDir
            }

            "assets_index_name" -> {
                assetsIndex
            }

            "auth_uuid" -> {
                minecraftUser.uuid()
            }

            "auth_xuid" -> {
                minecraftUser.xuid()
            }

            "auth_access_token" -> {
                minecraftUser.accessToken()
            }

            "clientid" -> {
                minecraftUser.clientId()
            }

            "user_type" -> {
                minecraftUser.type()
            }

            "version_type" -> {
                versionType
            }

            "resolution_width" -> {
                resX ?: ""
            }

            "resolution_height" -> {
                resY ?: ""
            }

            "quickPlayPath" -> {
                "quickPlay/log.json"
            }

            "quickPlaySingleplayer" -> {
                if (quickPlayData?.type == QuickPlayData.Type.WORLD) quickPlayData.name else ""
            }

            "quickPlayMultiplayer" -> {
                if (quickPlayData?.type == QuickPlayData.Type.SERVER) quickPlayData.name else ""
            }

            "quickPlayRealms" -> {
                if (quickPlayData?.type === QuickPlayData.Type.REALM) quickPlayData.name else ""
            }

            else -> throw GameCommandException("Unknown environment variable: key=$key")
        }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
