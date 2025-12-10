package com.example.meditox.utils

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDateTime

class LocalDateTimeDeserializer : JsonDeserializer<String> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): String {
        return when {
            json.isJsonArray -> {
                // Handle array format: [2025,12,10,19,11,54,268645017]
                val array = json.asJsonArray
                if (array.size() >= 6) {
                    val year = array[0].asInt
                    val month = array[1].asInt
                    val day = array[2].asInt
                    val hour = array[3].asInt
                    val minute = array[4].asInt
                    val second = array[5].asInt
                    val nano = if (array.size() > 6) array[6].asInt else 0
                    
                    val dateTime = LocalDateTime.of(year, month, day, hour, minute, second, nano)
                    return dateTime.toString()
                } else {
                    throw JsonParseException("Invalid LocalDateTime array format")
                }
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                // Handle string format (already correct)
                json.asString
            }
            else -> {
                throw JsonParseException("Invalid syncedAt format: expected array or string")
            }
        }
    }
}