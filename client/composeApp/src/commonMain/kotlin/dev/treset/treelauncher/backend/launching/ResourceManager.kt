package dev.treset.treelauncher.backend.launching

import io.github.oshai.kotlinlogging.KotlinLogging
import dev.treset.treelauncher.backend.data.InstanceData
import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.backend.util.exception.GameResourceException
import java.io.IOException
import java.time.LocalDateTime
import java.util.*

class ResourceManager(private var instanceData: InstanceData) {

    @Throws(IOException::class)
    fun prepareResources() {
        try {
            instanceData.mainManifest.activeInstance.value = instanceData.instance.id.value
            instanceData.mainManifest.write()
        } catch (e: IOException) {
            throw IOException("Failed to prepare resources: unable to set instance active", e)
        }
        try {
            prepareComponents(
                arrayOf(
                    instanceData.savesComponent.value,
                    instanceData.modsComponent.value ?: return,
                    instanceData.resourcepacksComponent.value ,
                    instanceData.optionsComponent.value,
                    instanceData.instance
                )
            )
        } catch (e: GameResourceException) {
            throw GameResourceException("Unable to prepare game resources", e)
        }
        LOGGER.info {"Prepared resources for launch, instance=${instanceData.instance.id}"}
    }

    @Throws(IOException::class)
    fun cleanupResources() {
        LOGGER.debug { "Cleaning up game files: instance=${instanceData.instance.id}" }
        try {
            cleanComponents(
                arrayOf(
                    instanceData.savesComponent.value,
                    instanceData.modsComponent.value ?: return,
                    instanceData.resourcepacksComponent.value,
                    instanceData.optionsComponent.value,
                    instanceData.instance
                )
            )
        } catch (e: GameResourceException) {
            throw GameResourceException("Unable to cleanup game files", e)
        }
        try {
            instanceData.mainManifest.activeInstance.value = null
            instanceData.mainManifest.write()
        } catch (e: IOException) {
            throw GameResourceException("Unable to cleanup game files: unable to set instance inactive", e)
        }
        LOGGER.info { "Game files cleaned up" }
    }

    @Throws(IOException::class)
    fun setLastPlayedTime() {
        LOGGER.debug { "Setting last played time: instance=${instanceData.instance.id}" }
        val time = LocalDateTime.now()
        instanceData.instance.lastUsedTime = time
        instanceData.savesComponent.value.lastUsedTime = time
        instanceData.resourcepacksComponent.value.lastUsedTime = time
        instanceData.optionsComponent.value.lastUsedTime = time
        instanceData.modsComponent.value?.lastUsedTime = time
        instanceData.javaComponent.value.lastUsedTime = time
        instanceData.versionComponents.forEach { it.lastUsedTime = time }
        instanceData.instance.write()
        instanceData.savesComponent.value.write()
        instanceData.resourcepacksComponent.value.write()
        instanceData.optionsComponent.value.write()
        instanceData.modsComponent.value?.write()
        instanceData.javaComponent.value.write()
        instanceData.versionComponents.forEach { it.write() }
        LOGGER.debug { "Set last played time: instance=${instanceData.instance.id}" }
    }

    @Throws(IOException::class)
    fun addPlayDuration(duration: Long) {
        LOGGER.debug { "Adding play duration: instance=${instanceData.instance.id}, duration=$duration" }

        instanceData.instance.totalTime.value += duration
        instanceData.instance.write()
        LOGGER.debug { "Added play duration: instance=${instanceData.instance.id}, duration=$duration, totalTime=${instanceData.instance.totalTime}" }
    }

    @Throws(IOException::class)
    private fun prepareComponents(components: Array<out Component>) {
        val undoable: MutableList<Component> = mutableListOf()
        for(component in components) {
            undoable.add(component)
            try {
                component.getResourceProvider(instanceData.gameDataDir).includeResources()
            } catch (e: IOException) {
                try {
                    cleanComponents(undoable.toTypedArray(), true)
                } catch (e2: IOException) {
                    throw IOException("Unable to clean resources after launch fail: component=${component.id}: $e2", e)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun cleanComponents(components: Array<out Component>, unexpected: Boolean = false) {
        val files = instanceData.gameDataDir.listFiles().toMutableList()

        val exceptions: MutableList<IOException> = mutableListOf()
        for(component in components) {
            try {
                component.getResourceProvider(instanceData.gameDataDir).removeResources(files, unexpected)
            } catch (e: IOException) {
                LOGGER.error(e) { "Unable to clean resources: component=${component.id}" }
                exceptions.add(e)
            }
        }
        if(exceptions.isNotEmpty()) {
            throw IOException("Unable to clean resources: ${exceptions.size} components", exceptions[0])
        }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
