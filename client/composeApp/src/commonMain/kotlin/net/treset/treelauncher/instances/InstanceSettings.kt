package net.treset.treelauncher.instances

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.sun.management.OperatingSystemMXBean
import net.treset.mc_version_loader.launcher.LauncherFeature
import net.treset.mc_version_loader.launcher.LauncherLaunchArgument
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.generic.TextBox
import net.treset.treelauncher.generic.TitledColumn
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.management.ManagementFactory
import java.util.stream.Collectors

@Composable
fun InstanceSettings(
    instance: InstanceData
) {
    val startMemory = remember(instance) { getCurrentMemory(instance) }
    var memory by remember(instance) { mutableStateOf(startMemory) }

    val startRes = remember(instance) { getResolution(instance) }
    var res by remember(instance) { mutableStateOf(startRes) }

    val startArgs = remember { instance.instance.second.jvm_arguments.filter { !it.argument.startsWith("-Xmx") && !it.argument.startsWith("-Xms") } }
    var args by remember { mutableStateOf(startArgs) }

    val systemMemory = remember { getSystemMemory() / 256 * 256 }

    DisposableEffect(instance) {
        onDispose {
            save(
                instance,
                memory,
                startMemory,
                res,
                startRes,
                args,
                startArgs
            )
        }
    }

    TitledColumn(
        title = strings().manager.instance.settings.title(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                strings().manager.instance.settings.memory(),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Slider(
                    value = memory.toFloat(),
                    valueRange = 256f..systemMemory.toFloat(),
                    onValueChange = {
                        memory = it.toInt()
                    },
                    steps = (systemMemory - 256) / 256 - 1,
                    modifier = Modifier.fillMaxWidth(2/3f)
                )

                TextBox(
                    text = memory.toString(),
                    onChange = {
                        memory = it.toIntOrNull()?.let { num -> if(num in 256..systemMemory) num else null} ?: memory
                    },
                    suffix = {
                        Text(strings().units.megabytes())
                    },
                    showClear = false
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                strings().manager.instance.settings.resolution(),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextBox(
                    text = res.first.toString(),
                    onChange = {
                        res = Pair(it.toIntOrNull()?.let { num -> if(num > 0) num else null} ?: res.first, res.second)
                    },
                    suffix = {
                        Text(strings().units.pixels())
                    },
                    showClear = false
                )

                Text(strings().units.resolutionBy())

                TextBox(
                    text = res.second.toString(),
                    onChange = {
                        res = Pair(res.first, it.toIntOrNull()?.let { num -> if(num > 0) num else null} ?: res.second)
                    },
                    suffix = {
                        Text(strings().units.pixels())
                    },
                    showClear = false
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                strings().manager.instance.settings.arguments(),
                style = MaterialTheme.typography.titleMedium
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(12.dp)
            ) {

                args.forEach {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {  }
                            .pointerHoverIcon(PointerIcon.Hand)
                            .padding(start = 8.dp)
                            .padding(4.dp)
                    ) {
                        Text(
                            it.argument
                        )
                        IconButton(
                            onClick = {
                                args = args.filter { arg -> arg != it }
                            },
                            interactionTint = MaterialTheme.colorScheme.error
                        ) {
                            Icon(
                                icons().delete,
                                "Edit Argument"
                            )
                        }
                    }

                }
            }

            var newArg by remember { mutableStateOf("") }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextBox(
                    text = newArg,
                    onChange = {
                        newArg = it
                    },
                    placeholder = strings().manager.instance.settings.argumentAdd(),
                )

                IconButton(
                    onClick = {
                        args = args + LauncherLaunchArgument(newArg, null, null, null, null)
                        newArg = ""
                    },
                    enabled = newArg.isNotBlank()
                ) {
                    Icon(
                        icons().add,
                        "Add Argument",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Throws(IOException::class)
private fun getCurrentMemory(instance: InstanceData): Int {
    for (argument in instance.instance.second.jvm_arguments) {
        if ((argument.argument.startsWith("-Xmx") || argument.argument.startsWith("-Xms")) && argument.argument.endsWith("m")
        ) {
            return argument.argument.replace("-Xmx", "").replace("m".toRegex(), "").toInt()
        }
    }

    val regex = PatternString("MaxHeapSize\\s*=\\s?(\\d+)", true)
    val result = BufferedReader(
        InputStreamReader(
            Runtime.getRuntime().exec(
                LauncherFile.of(instance.javaComponent.directory, "bin", "java").path
                        + " -XX:+PrintFlagsFinal -version | findstr HeapSize"
            ).inputStream
        )
    ).lines().collect(
        Collectors.joining(" ")
    )
    val match = regex.firstGroup(result)
    return match?.let {
        (it.toLong() / 1024 / 1024).toInt()
    } ?: 1024
}

private fun getSystemMemory(): Int {
    val bean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
    return (bean.totalMemorySize / 1024 / 1024).toInt()
}

private fun saveMemory(instance: InstanceData, memory: Int, startMemory: Int) {
    if (memory != startMemory) {
        val newArguments = ArrayList<LauncherLaunchArgument>()
        for (argument in instance.instance.second.jvm_arguments) {
            if (!argument.argument.startsWith("-Xmx") && !argument.argument.startsWith("-Xms")) {
                newArguments.add(argument)
            }
        }
        newArguments.add(LauncherLaunchArgument("-Xmx${memory}m", null, null, null, null))
        newArguments.add(LauncherLaunchArgument("-Xms${memory}m", null, null, null, null))
        instance.instance.second.jvm_arguments = newArguments
    }
}

private fun getResolution(instance: InstanceData): Pair<Int, Int> {
    val features = instance.instance.second.features
    val resX = features.firstOrNull { it.feature == "resolution_x" }
    val resY = features.firstOrNull { it.feature == "resolution_y" }

    return Pair(
        resX?.value?.toIntOrNull() ?: 854,
        resY?.value?.toIntOrNull() ?: 480
    )
}

private fun saveResolution(instance: InstanceData, res: Pair<Int, Int>, startRes: Pair<Int, Int>) {
    if (res != startRes) {
        val newFeatures = instance.instance.second.features
                .filter { f -> f.feature != "resolution_x" && f.feature != "resolution_y" }
                .toMutableList()
        newFeatures.add(LauncherFeature("resolution_x", res.first.toString()))
        newFeatures.add(LauncherFeature("resolution_y", res.second.toString()))
        instance.instance.second.features = newFeatures
    }
}

private fun saveArgs(instance: InstanceData, args: List<LauncherLaunchArgument>, startArgs: List<LauncherLaunchArgument>) {
    if (args != startArgs) {
        val mutArgs = args.toMutableList()
        for (argument in instance.instance.second.jvm_arguments) {
            if (argument.argument.startsWith("-Xmx") || argument.argument.startsWith("-Xms")) {
                mutArgs.add(argument)
            }
        }
        instance.instance.second.jvm_arguments = mutArgs
    }
}

@Throws(IOException::class)
private fun save(
    instance: InstanceData,
    memory: Int,
    startMemory: Int,
    res: Pair<Int, Int>,
    startRes: Pair<Int, Int>,
    args: List<LauncherLaunchArgument>,
    startArgs: List<LauncherLaunchArgument>
) {
    saveMemory(instance, memory, startMemory)
    saveResolution(instance, res, startRes)
    saveArgs(instance, args, startArgs)
    LauncherFile.of(
        instance.instance.first.directory,
        instance.instance.first.details
    ).write(instance.instance.second)
}