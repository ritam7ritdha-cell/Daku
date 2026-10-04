package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import java.util.concurrent.TimeUnit

// --- Data Models for Gemini REST API ---

data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val tools: List<Map<String, Any>>? = null,
    val systemInstruction: Content? = null
)

data class Content(
    val role: String? = null,
    val parts: List<Part>
)

data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

data class InlineData(
    val mimeType: String,
    val data: String
)

data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val thinkingConfig: ThinkingConfig? = null,
    val imageConfig: ImageConfig? = null,
    val responseModalities: List<String>? = null,
    val speechConfig: SpeechConfig? = null,
    val responseMimeType: String? = null
)

data class ThinkingConfig(
    val thinkingLevel: String // "HIGH" or "LOW"
)

data class ImageConfig(
    val aspectRatio: String? = "1:1",
    val imageSize: String? = "1K"
)

data class SpeechConfig(
    val voiceConfig: VoiceConfig? = null
)

data class VoiceConfig(
    val prebuiltVoiceConfig: PrebuiltVoiceConfig? = null
)

data class PrebuiltVoiceConfig(
    val voiceName: String? = "Kore"
)

data class GenerateContentResponse(
    val candidates: List<Candidate>? = null,
    val error: ApiError? = null
)

data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null
)

data class ApiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

// Veo Request & Response
data class GenerateVideosRequest(
    val prompt: String,
    val config: VeoConfig? = null
)

data class VeoConfig(
    val numberOfVideos: Int = 1,
    val resolution: String = "1080p",
    val aspectRatio: String = "16:9"
)

data class VeoResponse(
    val name: String? = null,
    val done: Boolean? = false
)

// --- Retrofit API Service ---

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/{model}:streamGenerateContent")
    @Streaming
    suspend fun generateContentStream(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): ResponseBody

    @POST("v1beta/models/{model}:generateVideos")
    suspend fun generateVideos(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateVideosRequest
    ): VeoResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }
}
