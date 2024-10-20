package dev.treset.treelauncher.backend.util.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

@OptIn(ExperimentalSerializationApi::class)
val Serializer = Json {
    prettyPrint = true
    namingStrategy = JsonNamingStrategy.SnakeCase
    isLenient = true
    ignoreUnknownKeys = true
    classDiscriminatorMode = ClassDiscriminatorMode.NONE
}