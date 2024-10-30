package dev.treset.treelauncher.components.resourcepacks

import dev.treset.treelauncher.backend.data.manifest.ResourcepackComponent
import dev.treset.treelauncher.backend.launching.resources.ResourcepacksDisplayData
import dev.treset.treelauncher.components.SharedAddableComponentData

class SharedResourcepacksData(
    component: ResourcepackComponent,
    reload: () -> Unit,
    var displayData: ResourcepacksDisplayData = ResourcepacksDisplayData(),
): SharedAddableComponentData<ResourcepackComponent>(
    component,
    reload
) {
    companion object {
        fun of(component: ResourcepackComponent, reload: () -> Unit): SharedResourcepacksData {
            return SharedResourcepacksData(component, reload)
        }
    }
}