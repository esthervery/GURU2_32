package com.android.guru2

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

data class CharacterGenerationResponse(
    val id: String,
    val status: String,
    val message: String
)
interface CharacterApiService {
    @FormUrlEncoded
    @POST("api/images/generate-from-url")
    suspend fun requestCharacterGeneration(
        @Field("user_id") userId: String,
        @Field("pet_image_url") imageUrl: String
    ): Response<CharacterGenerationResponse>
}