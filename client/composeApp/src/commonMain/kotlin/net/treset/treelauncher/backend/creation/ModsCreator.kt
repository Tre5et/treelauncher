package net.treset.treelauncher.backend.creation

import net.treset.treelauncher.backend.data.manifest.ModsComponent
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.util.FormatStringProvider
import net.treset.treelauncher.backend.util.Status

class ModsCreator(
    parent: ParentManifest,
    onStatus: (Status) -> Unit
) : ComponentCreator<ModsComponent, ModsCreationData>(parent, onStatus) {
    override fun createNew(data: ModsCreationData, statusProvider: CreationProvider): ModsComponent {
        return ModsComponent(
            id = id,
            name = data.name,
            types = data.types,
            versions = data.versions,
            file = file
        )
    }

    override fun createInherit(data: ModsCreationData, statusProvider: CreationProvider): ModsComponent {
        return createNew(data, statusProvider)
    }

    override val step = MODS
    override val newTotal = 0
    override val inheritTotal = 0
}

class ModsCreationData(
    name: String,
    val types: List<String> = listOf(),
    val versions: List<String> = listOf()
): CreationData(name)

val MODS = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.mods() }
