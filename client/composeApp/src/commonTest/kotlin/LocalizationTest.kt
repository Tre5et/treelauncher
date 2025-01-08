import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.localization.Strings
import kotlin.reflect.KClass
import kotlin.test.Test

internal class LocalizationTest {
    val defaultMembers = listOf(
        "component\\d+".toRegex(),
        "copy".toRegex(),
        "equals".toRegex(),
        "hashCode".toRegex(),
        "toString".toRegex()
    )

    @Test
    fun checkLocalizations() {
        val all = extractSubParameters(Strings::class)

        val de = extractSetParameters(LauncherFile.of("src/commonMain/kotlin/dev/treset/treelauncher/localization/StringsDe.kt"))

        val notSet = all - de.toSet()

        println("\nNOT IMPLEMENTED LOCALIZATION:")
        notSet.forEach {
            println(it)
        }
    }

    fun extractSubParameters(clazz: KClass<*>): List<String> {
        val members = clazz.members

        val list = mutableListOf<String>()
        members.forEach {
            if(defaultMembers.any { r -> r.matches(it.name) }) {
                return@forEach
            }
            if (it.returnType.classifier.toString().startsWith("class dev.treset.treelauncher.localization")) {
                list.addAll(
                    extractSubParameters(it.returnType.classifier as KClass<*>).map { p ->
                        "${it.name}.$p"
                    }
                )
            } else {
                list.add(it.name)
            }
        }

        return list
    }

    fun extractSetParameters(file: LauncherFile): List<String> {
        val lines = file.readLines()

        val used = mutableListOf<String>()
        val current = mutableListOf<String>()

        var inData = false
        var functionDepth = 0
        lines.forEach {
            if(functionDepth == 0) {
                if (it.contains('(')) {
                    if (inData) {
                        getName(it)?.let {
                            current.add(it)
                        }
                    }
                    inData = true
                }
                if (it.contains(')')) {
                    current.removeLastOrNull()
                }
            }
            if(it.contains('{')) {
                functionDepth++
                if (inData) {
                    getName(it)?.let {
                        used.add("${current.joinToString(".")}.$it")
                    }
                }
            }
            if(functionDepth != 0) {
                if (it.contains('}')) {
                    functionDepth--
                }
            }
        }

        return used
    }

    fun getName(line: String): String? {
        val endIndex = line.indexOf('=')
        if(endIndex <= 0) {
            return null
        }
        return line.substring(0, endIndex).trim()
    }
}