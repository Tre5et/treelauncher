package net.treset.treelauncher.backend.creation

import net.treset.treelauncher.backend.data.manifest.OptionsComponent
import net.treset.treelauncher.backend.util.StatusProvider
import net.treset.treelauncher.backend.util.FormatStringProvider
import net.treset.treelauncher.backend.util.Status

class NewOptionCreator(
    data: NewCreationData,
    statusProvider: StatusProvider
): NewComponentCreator<OptionsComponent, NewCreationData>(data, statusProvider) {
    constructor(
        data: NewCreationData,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    override fun createNew(statusProvider: StatusProvider): OptionsComponent {
        return OptionsComponent(
            id = id,
            name = data.name,
            file = file
        )
    }

    override val step = CreationStep.OPTIONS
}

class InheritOptionsCreator(
    data: InheritCreationData<OptionsComponent>,
    statusProvider: StatusProvider
): InheritComponentCreator<OptionsComponent, InheritCreationData<OptionsComponent>>(data, statusProvider) {
    constructor(
        data: InheritCreationData<OptionsComponent>,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    override fun createInherit(statusProvider: StatusProvider): OptionsComponent {
        return OptionsComponent(
            id = id,
            name = data.name,
            file = file
        )
    }

    override val step = CreationStep.OPTIONS
}

class UseOptionsCreator(
    data: UseCreationData<OptionsComponent>,
    statusProvider: StatusProvider
): UseComponentCreator<OptionsComponent, UseCreationData<OptionsComponent>>(data, statusProvider) {
    constructor(
        data: UseCreationData<OptionsComponent>,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    override val step = CreationStep.OPTIONS
}

object OptionsCreator {
    fun new(data: NewCreationData, onStatus: (Status) -> Unit): NewOptionCreator {
        return NewOptionCreator(data, onStatus)
    }

    fun inherit(data: InheritCreationData<OptionsComponent>, onStatus: (Status) -> Unit): InheritOptionsCreator {
        return InheritOptionsCreator(data, onStatus)
    }

    fun use(data: UseCreationData<OptionsComponent>, onStatus: (Status) -> Unit): UseOptionsCreator {
        return UseOptionsCreator(data, onStatus)
    }
}

val CreationStep.OPTIONS: FormatStringProvider
    get() = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.options() }
