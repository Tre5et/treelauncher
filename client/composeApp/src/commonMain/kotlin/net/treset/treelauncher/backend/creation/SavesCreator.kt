package net.treset.treelauncher.backend.creation

import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.data.manifest.SavesComponent
import net.treset.treelauncher.backend.util.FormatStringProvider
import net.treset.treelauncher.backend.util.Status

class SavesCreator(
    parent: ParentManifest,
    onStatus: (Status) -> Unit
) : ComponentCreator<SavesComponent, CreationData>(parent, onStatus) {
    override fun createNew(data: CreationData, statusProvider: CreationProvider): SavesComponent {
        return SavesComponent(
            id = id,
            name = data.name,
            file = file
        )
    }

    override fun createInherit(data: CreationData, statusProvider: CreationProvider): SavesComponent {
        return createNew(data, statusProvider)
    }

    override val step = CreationStep.SAVES
    override val newTotal = 0
    override val inheritTotal = 0
}

val CreationStep.SAVES: FormatStringProvider
    get() = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.saves() }
