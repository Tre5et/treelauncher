package net.treset.treelauncher.backend.creation

import net.treset.treelauncher.backend.data.manifest.OptionsComponent
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.util.FormatStringProvider
import net.treset.treelauncher.backend.util.Status

class OptionsCreator(
    parent: ParentManifest,
    onStatus: (Status) -> Unit
) : ComponentCreator<OptionsComponent, CreationData>(parent, onStatus) {
    override fun createNew(data: CreationData, statusProvider: CreationProvider): OptionsComponent {
        return OptionsComponent(
            id = id,
            name = data.name,
            file = file
        )
    }

    override fun createInherit(data: CreationData, statusProvider: CreationProvider): OptionsComponent {
        return createNew(data, statusProvider)
    }

    override val step = CreationStep.OPTIONS
    override val newTotal = 0
    override val inheritTotal = 0
}

val CreationStep.OPTIONS: FormatStringProvider
    get() = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.options() }
