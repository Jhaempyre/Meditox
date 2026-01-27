package com.example.meditox.utils

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GsonLocalDateTimeAdapter : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    override fun serialize(src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDateTime {
        if (json == null || json.isJsonNull) {
            throw JsonParseException("JSON is null")
        }

        return when {
            json.isJsonArray -> {
                val array = json.asJsonArray
                if (array.size() >= 5) {
                    val year = array[0].asInt
                    val month = array[1].asInt
                    val day = array[2].asInt
                    val hour = array[3].asInt
                    val minute = array[4].asInt
                    val second = if (array.size() > 5) array[5].asInt else 0
                    val nano = if (array.size() > 6) array[6].asInt else 0
                    LocalDateTime.of(year, month, day, hour, minute, second, nano)
                } else {
                    throw JsonParseException("Invalid LocalDateTime array format: $json")
                }
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                LocalDateTime.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
            else -> {
                throw JsonParseException("Unexpected JSON type for LocalDateTime: $json")
            }
        }
    }
}
