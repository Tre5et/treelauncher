package dev.treset.treelauncher.instances

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.sun.management.OperatingSystemMXBean
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.InstanceData
import dev.treset.treelauncher.backend.data.LauncherFeature
import dev.treset.treelauncher.backend.data.LauncherLaunchArgument
import dev.treset.treelauncher.backend.util.assignFrom
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.string.PatternString
import dev.treset.treelauncher.generic.IconButton
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.generic.TextBox
import dev.treset.treelauncher.generic.TitledColumn
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.management.ManagementFactory
import java.util.stream.Collectors

@Composable
fun InstanceSettings(
    instance: InstanceData
) {
    var startMemory: Int? = remember(instance) { null }
    var memory by remember(startMemory) {
        mutableStateOf(startMemory)
    }

    val startRes = remember(instance) { getResolution(instance) }
    var res by remember(instance) { mutableStateOf(startRes) }

    val startArgs = remember { instance.instance.jvmArguments.filter { !it.argument.startsWith("-Xmx") && !it.argument.startsWith("-Xms") } }
    var args by remember { mutableStateOf(startArgs) }

    var systemMemory: Int? by remember { mutableStateOf(null) }

    DisposableEffect(instance) {
        Thread {
            try {
                startMemory = getCurrentMemory(instance)
                memory = startMemory
            } catch(e: IOException) {
                AppContext.error(e)
            }
        }.start()

        Thread {
            systemMemory = getSystemMemory() / 256 * 256
        }.start()

        onDispose {
            memory?.let { mem ->
                startMemory?.let { startMem ->
                    try {
                        save(
                            instance,
                            mem,
                            startMem,
                            res,
                            startRes,
                            args,
                            startArgs
                        )
                    } catch (e: IOException) {
                        AppContext.error(e)
                    }
                }
            }
        }
    }

    TitledColumn(
        title = Strings.manager.instance.settings.title(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                Strings.manager.instance.settings.memory(),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Slider(
                    value = memory?.toFloat()?: 256f,
                    valueRange = 256f..(systemMemory?.toFloat()?: 256f),
                    onValueChange = {
                        memory = it.toInt()
                    },
                    steps = systemMemory?.let { (it - 256) / 256 - 1 } ?: 0,
                    modifier = Modifier.fillMaxWidth(2/3f),
                    enabled = systemMemory != null && memory != null
                )

                TextBox(
                    text = memory.toString(),
                    onTextChanged = {
                        memory = it.toIntOrNull()?.let { num -> if(num in 256..(systemMemory ?: 256)) num else null} ?: memory
                    },
                    suffix = {
                        Text(Strings.units.megabytes())
                    },
                    enabled = systemMemory != null && memory != null
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                Strings.manager.instance.settings.resolution(),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextBox(
                    text = res.first.toString(),
                    onTextChanged = {
                        res = Pair(it.toIntOrNull()?.let { num -> if(num > 0) num else null} ?: res.first, res.second)
                    },
                    suffix = {
                        Text(Strings.units.pixels())
                    }
                )

                Text(Strings.units.resolutionBy())

                TextBox(
                    text = res.second.toString(),
                    onTextChanged = {
                        res = Pair(res.first, it.toIntOrNull()?.let { num -> if(num > 0) num else null} ?: res.second)
                    },
                    suffix = {
                        Text(Strings.units.pixels())
                    }
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                Strings.manager.instance.settings.arguments(),
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
                            icon = icons().delete,
                            interactionTint = MaterialTheme.colorScheme.error,
                            tooltip = Strings.manager.instance.settings.deleteArgument()
                        )
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
                    onTextChanged = {
                        newArg = it
                    },
                    placeholder = Strings.manager.instance.settings.argumentPlaceholder(),
                )

                IconButton(
                    onClick = {
                        args = args + LauncherLaunchArgument(newArg)
                        newArg = ""
                    },
                    icon = icons().add,
                    size = 32.dp,
                    enabled = newArg.isNotBlank(),
                    tooltip = Strings.manager.instance.settings.addArgument()
                )
            }
        }
    }
}

@Throws(IOException::class)
private fun getCurrentMemory(instance: InstanceData): Int {
    for (argument in instance.instance.jvmArguments) {
        if ((argument.argument.startsWith("-Xmx") || argument.argument.startsWith("-Xms")) && argument.argument.endsWith("m")
        ) {
            try {
                return argument.argument.replace("-Xmx", "").replace("m".toRegex(), "").toInt()
            } catch (_: NumberFormatException) { }
        }
    }

    val regex = PatternString("MaxHeapSize\\s*=\\s?(\\d+)", true)
    val result = BufferedReader(
        InputStreamReader(
            Runtime.getRuntime().exec(
                LauncherFile.of(instance.javaComponent.value.directory, "bin", "java").path
                        + " -XX:+PrintFlagsFinal -version | findstr HeapSize"
            ).inputStream
        )
    ).lines().collect(
        Collectors.joining(" ")
    )
    val match = regex.firstGroup(result)
    return match?.let {
        try {
            (it.toLong() / 1024 / 1024).toInt()
        } catch (_: NumberFormatException) {
            null
        }
    } ?: 1024
}

private fun getSystemMemory(): Int {
    val bean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
    return (bean.totalMemorySize / 1024 / 1024).toInt()
}

private fun saveMemory(instance: InstanceData, memory: Int, startMemory: Int) {
    if (memory != startMemory) {
        val newArguments = mutableListOf<LauncherLaunchArgument>()
        for (argument in instance.instance.jvmArguments) {
            if (!argument.argument.startsWith("-Xmx") && !argument.argument.startsWith("-Xms")) {
                newArguments.add(argument)
            }
        }
        newArguments.add(LauncherLaunchArgument("-Xmx${memory}m", null, null, null, null))
        newArguments.add(LauncherLaunchArgument("-Xms${memory}m", null, null, null, null))
        instance.instance.jvmArguments.assignFrom(newArguments)
    }
}

private fun getResolution(instance: InstanceData): Pair<Int, Int> {
    val features = instance.instance.features
    val resX = features.firstOrNull { it.feature == "resolution_x" }
    val resY = features.firstOrNull { it.feature == "resolution_y" }

    return Pair(
        resX?.value?.toIntOrNull() ?: 854,
        resY?.value?.toIntOrNull() ?: 480
    )
}

private fun saveResolution(instance: InstanceData, res: Pair<Int, Int>, startRes: Pair<Int, Int>) {
    if (res != startRes) {
        val newFeatures = instance.instance.features
                .filter { f -> f.feature != "resolution_x" && f.feature != "resolution_y" }
                .toMutableList()
        newFeatures.add(LauncherFeature("resolution_x", res.first.toString()))
        newFeatures.add(LauncherFeature("resolution_y", res.second.toString()))
        instance.instance.features.assignFrom(newFeatures)
    }
}

private fun saveArgs(instance: InstanceData, args: List<LauncherLaunchArgument>, startArgs: List<LauncherLaunchArgument>) {
    if (args != startArgs) {
        val mutArgs = args.toMutableList()
        for (argument in instance.instance.jvmArguments) {
            if (argument.argument.startsWith("-Xmx") || argument.argument.startsWith("-Xms")) {
                mutArgs.add(argument)
            }
        }
        instance.instance.jvmArguments.assignFrom(mutArgs)
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
    instance.instance.write()
}