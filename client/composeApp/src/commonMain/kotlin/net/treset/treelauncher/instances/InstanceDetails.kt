package net.treset.treelauncher.instances

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import net.treset.treelauncher.backend.data.InstanceData

@Composable
fun InstanceDetails(instance: InstanceData) {
    //TODO
    Text(instance.instance.first.id)
}