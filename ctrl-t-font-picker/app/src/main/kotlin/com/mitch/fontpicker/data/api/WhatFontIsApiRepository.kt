package com.mitch.fontpicker.data.api

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import timber.log.Timber

class WhatFontIsApiRepository(
    private val httpClient: HttpClient,
    private val apiKey: String
) {
    /**
     * Sends a Base64-encoded image to the WhatFontIs API for font identification.
     *
     * @param imageBase64 Base64-encoded image string.
     * @param limit The maximum number of fonts to return (default: 20).
     * @return A list of [FontResult] objects.
     * @throws IllegalStateException if the API returns a non-success status or a non-whitelisted plain text response.
     */
    @Suppress("SpellCheckingInspection") // Suppress IDE spell check warnings in this function
    suspend fun identifyFont(imageBase64: String, limit: Int = 5): List<FontResult> {
        val apiUrl = "https://www.whatfontis.com/api2/"

        val params = Parameters.build {
            append("API_KEY", apiKey)
            append("IMAGEBASE64", "1") // Use Base64-encoded image
            append("NOTTEXTBOXSDETECTION", "0") // Detect text boxes
            append("urlimagebase64", imageBase64)
            append("limit", limit.toString())
        }

        val response: HttpResponse = httpClient.post(apiUrl) {
            headers {
                append("Accept", "application/json")
            }
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(FormDataContent(params))
        }


        // Force treat the response body as text
        val responseBody = response.bodyAsText()
        Timber.d("Raw response body: $responseBody")

        try {
            // Attempt to decode as JSON
            return kotlinx.serialization.json.Json.decodeFromString(responseBody)
        } catch (jsonException: Exception) {
            if (responseBody == "No chars found") {
                Timber.d("No characters found in the image.")
                return emptyList()
            } else {
                Timber.w(jsonException, "Response is not valid JSON.")
                throw IllegalStateException("API error encounterd ($responseBody)")
            }

        }
    }
}

@Serializable
data class FontResult(
    @SerialName("title") val title: String,
    @SerialName("url") val url: String,
    @SerialName("image") val imageUrl0: String,
    @SerialName("image1") val imageUrl1: String,
    @SerialName("image2") val imageUrl2: String
)

data class FontDownloaded(
    val title: String,
    val url: String,
    val imageUrls: List<String>,
    val bitmaps: List<Bitmap>,
    val isLiked: MutableState<Boolean> = mutableStateOf(false),
    val id: Int? = null
)
