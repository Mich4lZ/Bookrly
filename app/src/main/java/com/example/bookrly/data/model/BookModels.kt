package com.example.bookrly.data.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class SubjectResponse(
    @SerializedName("works") val works: List<WorkDto>
)

data class WorkDto(
    @SerializedName("key") val key: String,
    @SerializedName("title") val title: String,
    @SerializedName("cover_id") val coverId: Long?,
    @SerializedName("authors") val authors: List<AuthorDto>?
)

data class AuthorDto(
    @SerializedName("name") val name: String
)

data class WorkDetailDto(
    @SerializedName("title")
    val title: String?,

    @SerializedName("covers")
    val covers: List<Long>?,

    @SerializedName("description") 
    @JsonAdapter(DescriptionDeserializer::class)
    val description: String?,
    
    @SerializedName("first_publish_date") 
    val firstPublishDate: String?,
    
    @SerializedName("number_of_pages") 
    val numberOfPages: Int?
)

class DescriptionDeserializer : JsonDeserializer<String> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): String {
        return if (json.isJsonObject) {
            json.asJsonObject.get("value").asString
        } else {
            json.asString
        }
    }
}

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val description: String? = null,
    val publishYear: String? = null,
    val pages: Int? = null
)
