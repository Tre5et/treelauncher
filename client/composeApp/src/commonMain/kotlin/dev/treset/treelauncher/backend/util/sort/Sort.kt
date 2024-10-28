package dev.treset.treelauncher.backend.util.sort

import androidx.compose.runtime.mutableStateOf
import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

@Serializable
class Sort<T>(
    val type: MutableDataState<ComparatorData<T>>,
    val reverse: MutableDataState<Boolean>
) {
    constructor(
        type: ComparatorData<T>,
        reverse: Boolean
    ): this(
        mutableStateOf(type),
        mutableStateOf(reverse)
    )

    fun <E: T> sort(list: List<E>): List<E> {
        val new = list.sortedWith(type.value).let {
            if (reverse.value) {
                it.reversed()
            } else {
                it
            }
        }
        return new
    }
}

fun <T> List<T>.sorted(sort: Sort<in T>): List<T> {
    return sort.sort(this)
}

typealias ComparatorData<T> = @Serializable(with = ComparatorSerializer::class) Comparator<T>

class ComparatorSerializer<T>: KSerializer<Comparator<T>> {
    override val descriptor = PrimitiveSerialDescriptor("Comparator", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Comparator<T> {
        val id = decoder.decodeString()
        val clazz = sorts[id] ?: throw SerializationException("Unknown sort type: $id")
        val instance = clazz.constructors.first().call()
        try {
            @Suppress("UNCHECKED_CAST")
            return instance as Comparator<T>
        } catch (e: ClassCastException) {
            throw SerializationException("Invalid sort type: $id")
        }
    }
    override fun serialize(encoder: Encoder, value: Comparator<T>) {
        for((id, clazz) in sorts) {
            if(clazz.isInstance(value)) {
                encoder.encodeString(id)
                return
            }
        }
        throw IllegalArgumentException("Unknown sort type: $value")
    }
}

val sorts: Map<String, KClass<out Comparator<*>>> = mapOf(
    "NAME" to ComponentNameComparator::class,
    "LAST_USED" to ComponentLastUsedComparator::class,
    "TIME_PLAYED" to InstanceComponentTimePlayedComparator::class,
    "MOD_NAME" to LauncherModNameComparator::class,
    "MOD_ENABLED_NAME" to LauncherModEnabledNameComparator::class
)

enum class InstanceSortType(val comparator: Comparator<Component>) {
    NAME(ComponentNameComparator()),
    LAST_PLAYED(ComponentLastUsedComparator()),
    TIME(InstanceComponentTimePlayedComparator());

    override fun toString(): String {
        return comparator.toString()
    }
}

enum class LauncherModSortType(val comparator: Comparator<LauncherMod>) {
    NAME(LauncherModNameComparator()),
    DISABLED_NAME(LauncherModEnabledNameComparator());

    override fun toString(): String {
        return comparator.toString()
    }
}

enum class ComponentSortType(val comparator: Comparator<Component>) {
    NAME(ComponentNameComparator()),
    LAST_USED(ComponentLastUsedComparator());

    override fun toString(): String {
        return comparator.toString()
    }
}