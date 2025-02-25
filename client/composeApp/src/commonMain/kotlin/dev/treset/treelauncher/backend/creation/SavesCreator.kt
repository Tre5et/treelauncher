package dev.treset.treelauncher.backend.creation

import dev.treset.treelauncher.backend.data.manifest.SavesComponent
import dev.treset.treelauncher.backend.util.FormatStringProvider
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.backend.util.StatusProvider
import dev.treset.treelauncher.localization.Strings

class NewSavesCreator(
    data: NewCreationData,
    statusProvider: StatusProvider
): NewComponentCreator<SavesComponent, NewCreationData>(data, statusProvider) {
    constructor(
        data: NewCreationData,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    override fun createNew(statusProvider: StatusProvider): SavesComponent {
        return SavesComponent(
            id = id,
            name = data.name,
            file = file
        )
    }

    override val step = CreationStep.SAVES
}

class InheritSavesCreator(
    data: InheritCreationData<SavesComponent>,
    statusProvider: StatusProvider
): InheritComponentCreator<SavesComponent, InheritCreationData<SavesComponent>>(data, statusProvider) {
    constructor(
        data: InheritCreationData<SavesComponent>,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    override fun createInherit(statusProvider: StatusProvider): SavesComponent {
        return SavesComponent(
            id = id,
            name = data.name,
            file = file
        )
    }

    override val step = CreationStep.SAVES
}

class UseSaveCreator(
    data: UseCreationData<SavesComponent>,
    statusProvider: StatusProvider
): UseComponentCreator<SavesComponent, UseCreationData<SavesComponent>>(data, statusProvider) {
    constructor(
        data: UseCreationData<SavesComponent>,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    override val step = CreationStep.SAVES
}

object SavesCreator {
    fun new(data: NewCreationData, onStatus: (Status) -> Unit): NewSavesCreator {
        return NewSavesCreator(data, onStatus)
    }

    fun inherit(data: InheritCreationData<SavesComponent>, onStatus: (Status) -> Unit): InheritSavesCreator {
        return InheritSavesCreator(data, onStatus)
    }

    fun use(data: UseCreationData<SavesComponent>, onStatus: (Status) -> Unit): UseSaveCreator {
        return UseSaveCreator(data, onStatus)
    }
}

val CreationStep.SAVES: FormatStringProvider
    get() = FormatStringProvider { Strings.creator.status.saves() }
