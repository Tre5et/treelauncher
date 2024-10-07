package net.treset.treelauncher.backend.creation

import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.data.manifest.ResourcepackComponent
import net.treset.treelauncher.backend.util.FormatStringProvider
import net.treset.treelauncher.backend.util.Status
import net.treset.treelauncher.backend.util.StringProvider

class ResourcepackCreator(
    parent: ParentManifest,
    onStatus: (Status) -> Unit
) : ComponentCreator<ResourcepackComponent, CreationData>(parent, onStatus) {
    override fun createNew(data: CreationData, statusProvider: CreationProvider): ResourcepackComponent {
        return ResourcepackComponent(
            id = id,
            name = data.name,
            file = file
        )
    }

    override fun createInherit(data: CreationData, statusProvider: CreationProvider): ResourcepackComponent {
        return createNew(data, statusProvider)
    }

    override val step: StringProvider = CreationStep.RESOURCEPACKS
    override val newTotal: Int = 0
    override val inheritTotal: Int = 0

}

val CreationStep.RESOURCEPACKS: FormatStringProvider
    get() = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.resourcepacks() }
