package com.example.meditox.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object LocalDateTimeKSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        // Handle array format: [year, month, day, hour, minute, second, nano]
        // We use JsonDecoder to access JsonElement if possible, or just decode as a structure
        // Since we know the input is an array, we can decode it element by element using a list-like approach
        // or decoding as a composite.
        
        // However, standard KSerializer 'deserialize' handles primitives or structures.
        // For an array [Y, M, D, ...], we can decode it as a List<Int> first.
        
        // A simpler approach with kotlinx.serialization for random JSON structures is using JsonElement
        // but that requires 'decoder' to be a 'JsonDecoder'.
        
        val input = decoder as? kotlinx.serialization.json.JsonDecoder 
            ?: throw kotlinx.serialization.SerializationException("This class can be loaded only by Json")
        
        val element = input.decodeJsonElement()
        
        if (element is kotlinx.serialization.json.JsonArray) {
             val array = element
             if (array.size >= 5) {
                 val year = array[0].toString().toInt()
                 val month = array[1].toString().toInt()
                 val day = array[2].toString().toInt()
                 val hour = array[3].toString().toInt()
                 val minute = array[4].toString().toInt()
                 val second = if (array.size > 5) array[5].toString().toInt() else 0
                 val nano = if (array.size > 6) array[6].toString().toInt() else 0
                 return LocalDateTime.of(year, month, day, hour, minute, second, nano)
             }
        } else if (element is kotlinx.serialization.json.JsonPrimitive) {
             // Fallback to string ISO format if it's not an array
             if (element.isString) {
                 return LocalDateTime.parse(element.content, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
             }
        }
        
        throw kotlinx.serialization.SerializationException("Expected JsonArray or ISO String for LocalDateTime, got $element")
    }
}
