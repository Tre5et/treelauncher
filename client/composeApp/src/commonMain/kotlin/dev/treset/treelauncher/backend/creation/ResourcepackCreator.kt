package dev.treset.treelauncher.backend.creation

import dev.treset.treelauncher.backend.data.manifest.ResourcepackComponent
import dev.treset.treelauncher.backend.util.FormatStringProvider
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.backend.util.StatusProvider
import dev.treset.treelauncher.localization.Strings

class NewResourcepackCreator(
    data: NewCreationData,
    statusProvider: StatusProvider
) : NewComponentCreator<ResourcepackComponent, NewCreationData>(data, statusProvider) {
    constructor(
        data: NewCreationData,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    override fun createNew(statusProvider: StatusProvider): ResourcepackComponent {
        return ResourcepackComponent(
            id = id,
            name = data.name,
            file = file
        )
    }

    override val step = CreationStep.RESOURCEPACKS
}

class InheritResourcepackCreator(
    data: InheritCreationData<ResourcepackComponent>,
    statusProvider: StatusProvider
) : InheritComponentCreator<ResourcepackComponent, InheritCreationData<ResourcepackComponent>>(data, statusProvider) {
    constructor(
        data: InheritCreationData<ResourcepackComponent>,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    override fun createInherit(statusProvider: StatusProvider): ResourcepackComponent {
        return ResourcepackComponent(
            id = id,
            name = data.name,
            file = file
        )
    }

    override val step = CreationStep.RESOURCEPACKS
}

class UseResourcepackCreator(
    data: UseCreationData<ResourcepackComponent>,
    statusProvider: StatusProvider
) : UseComponentCreator<ResourcepackComponent, UseCreationData<ResourcepackComponent>>(data, statusProvider) {
    constructor(
        data: UseCreationData<ResourcepackComponent>,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    override val step = CreationStep.RESOURCEPACKS
}

object ResourcepackCreator {
    fun new(data: NewCreationData, onStatus: (Status) -> Unit): NewResourcepackCreator {
        return NewResourcepackCreator(data, onStatus)
    }

    fun inherit(data: InheritCreationData<ResourcepackComponent>, onStatus: (Status) -> Unit): InheritResourcepackCreator {
        return InheritResourcepackCreator(data, onStatus)
    }

    fun use(data: UseCreationData<ResourcepackComponent>, onStatus: (Status) -> Unit): UseResourcepackCreator {
        return UseResourcepackCreator(data, onStatus)
    }
}

val CreationStep.RESOURCEPACKS: FormatStringProvider
    get() = FormatStringProvider { Strings.creator.status.resourcepacks() }
