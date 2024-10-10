package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.treelauncher.backend.data.manifest.*
import net.treset.treelauncher.backend.util.StatusProvider
import net.treset.treelauncher.backend.util.FormatStringProvider
import net.treset.treelauncher.backend.util.Status
import net.treset.treelauncher.backend.util.exception.ComponentCreationException
import net.treset.treelauncher.localization.strings
import java.io.IOException

class InstanceCreator(
    data: InstanceCreationData,
    statusProvider: StatusProvider
) : NewComponentCreator<InstanceComponent, InstanceCreationData>(data, statusProvider) {
    constructor(
        data: InstanceCreationData,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))


    @Throws(ComponentCreationException::class)
    override fun createNew(statusProvider: StatusProvider): InstanceComponent {
        LOGGER.debug { "Creating new instance: name=${data.name}..." }
        statusProvider.next("Creating instance components") // TODO: make localized

        try {
            val version = data.createVersion(statusProvider)
            val saves = data.createSaves(statusProvider)
            val resourcepack = data.createResourcepack(statusProvider)
            val options = data.createOptions(statusProvider)
            val mods = data.createMods?.invoke(statusProvider)

            return InstanceComponent(
                id = id,
                name = data.name,
                versionComponent = version.id,
                savesComponent = saves.id,
                resourcepacksComponent = resourcepack.id,
                optionsComponent = options.id,
                modsComponent = mods?.id,
                file = file,
            )
        } catch (e: IOException) {
            // TODO: cleanup newly created components
            throw ComponentCreationException("Failed to create instance", e)
        }
    }

    override val step = CreationStep.INSTANCE
    override val total = 1

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}

class InstanceCreationData: NewCreationData {
    val createVersion: (StatusProvider) -> VersionComponent
    val createSaves: (StatusProvider) -> SavesComponent
    val createResourcepack: (StatusProvider) -> ResourcepackComponent
    val createOptions: (StatusProvider) -> OptionsComponent
    val createMods: ((StatusProvider) -> ModsComponent)?

    constructor(
        name: String,
        versionCreator: ComponentCreator<VersionComponent, *>,
        savesCreator: ComponentCreator<SavesComponent, *>,
        resourcepackCreator: ComponentCreator<ResourcepackComponent, *>,
        optionsCreator: ComponentCreator<OptionsComponent, *>,
        modsCreator: ComponentCreator<ModsComponent, *>?,
        parent: ParentManifest
    ) : super(name, parent) {
        createVersion = { versionCreator.apply { statusProvider = it }.create() }
        createSaves = { savesCreator.apply { statusProvider = it }.create() }
        createResourcepack = { resourcepackCreator.apply { statusProvider = it }.create() }
        createOptions = { optionsCreator.apply { statusProvider = it }.create() }
        createMods = modsCreator?.let { m -> { m.apply { statusProvider = it }.create() } }
    }

    constructor(
        name: String,
        versionComponent: VersionComponent,
        savesComponent: SavesComponent,
        resourcepackComponent: ResourcepackComponent,
        optionsComponent: OptionsComponent,
        modsComponent: ModsComponent?,
        parent: ParentManifest
    ): super(name, parent) {
        createVersion = { versionComponent }
        createSaves = { savesComponent }
        createResourcepack = { resourcepackComponent }
        createOptions = { optionsComponent }
        createMods = modsComponent?.let { m -> { m } }
    }
}

val CreationStep.INSTANCE: FormatStringProvider
    get() = FormatStringProvider { strings().creator.status.instance() }
