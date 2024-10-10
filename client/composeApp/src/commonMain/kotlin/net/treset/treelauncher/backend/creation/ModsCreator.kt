package net.treset.treelauncher.backend.creation

import net.treset.treelauncher.backend.data.manifest.ModsComponent
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.util.FormatStringProvider
import net.treset.treelauncher.backend.util.Status

class NewModsCreator(
    data: NewModsCreationData,
    statusProvider: CreationProvider
) : NewComponentCreator<ModsComponent, NewModsCreationData>(data, statusProvider) {
    constructor(
        data: NewModsCreationData,
        onStatus: (Status) -> Unit
    ) : this(data, CreationProvider(null, 0, onStatus))

    override fun createNew(statusProvider: CreationProvider): ModsComponent {
        return ModsComponent(
            id = id,
            name = data.name,
            types = data.types,
            versions = data.versions,
            file = file
        )
    }

    override val step = CreationStep.MODS
}

class InheritModsCreator(
    data: InheritModsCreationData,
    statusProvider: CreationProvider
) : InheritComponentCreator<ModsComponent, InheritModsCreationData>(data, statusProvider) {
    constructor(
        data: InheritModsCreationData,
        onStatus: (Status) -> Unit
    ) : this(data, CreationProvider(null, 0, onStatus))

    override fun createInherit(statusProvider: CreationProvider): ModsComponent {
        return ModsComponent(
            id = id,
            name = data.name,
            types = data.component.types,
            versions = data.component.versions,
            file = file
        )
    }

    override val step = CreationStep.MODS
}

class UseModsCreator(
    data: UseModsCreationData,
    statusProvider: CreationProvider
) : UseComponentCreator<ModsComponent, UseModsCreationData>(data, statusProvider) {
    constructor(
        data: UseModsCreationData,
        onStatus: (Status) -> Unit
    ) : this(data, CreationProvider(null, 0, onStatus))

    override val step = CreationStep.MODS
}

object ModsCreator {
    fun new(data: NewModsCreationData, onStatus: (Status) -> Unit): NewModsCreator {
        return NewModsCreator(data, onStatus)
    }

    fun inherit(data: InheritModsCreationData, onStatus: (Status) -> Unit): InheritModsCreator {
        return InheritModsCreator(data, onStatus)
    }

    fun use(data: UseModsCreationData, onStatus: (Status) -> Unit): UseModsCreator {
        return UseModsCreator(data, onStatus)
    }
}

class NewModsCreationData(
    name: String,
    val types: List<String> = listOf(),
    val versions: List<String> = listOf(),
    parent: ParentManifest
): NewCreationData(name, parent)

class InheritModsCreationData(
    name: String,
    component: ModsComponent,
    parent: ParentManifest
): InheritCreationData<ModsComponent>(name, component, parent)

class UseModsCreationData(
    component: ModsComponent,
    parent: ParentManifest
): UseCreationData<ModsComponent>(component, parent)

val CreationStep.MODS: FormatStringProvider
    get() = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.mods() }
