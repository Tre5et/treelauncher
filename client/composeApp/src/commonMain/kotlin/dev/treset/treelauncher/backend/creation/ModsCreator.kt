package dev.treset.treelauncher.backend.creation

import dev.treset.treelauncher.backend.data.manifest.ModsComponent
import dev.treset.treelauncher.backend.data.manifest.ParentManifest
import dev.treset.treelauncher.backend.util.FormatStringProvider
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.backend.util.StatusProvider
import dev.treset.treelauncher.localization.strings

class NewModsCreator(
    data: NewModsCreationData,
    statusProvider: StatusProvider
) : NewComponentCreator<ModsComponent, NewModsCreationData>(data, statusProvider) {
    constructor(
        data: NewModsCreationData,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    override fun createNew(statusProvider: StatusProvider): ModsComponent {
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
    statusProvider: StatusProvider
) : InheritComponentCreator<ModsComponent, InheritModsCreationData>(data, statusProvider) {
    constructor(
        data: InheritModsCreationData,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    override fun createInherit(statusProvider: StatusProvider): ModsComponent {
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
    statusProvider: StatusProvider
) : UseComponentCreator<ModsComponent, UseModsCreationData>(data, statusProvider) {
    constructor(
        data: UseModsCreationData,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

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
    get() = FormatStringProvider { strings().creator.status.mods() }
