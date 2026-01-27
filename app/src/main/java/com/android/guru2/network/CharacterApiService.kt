package com.android.guru2.network

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

data class CharacterGenerationResponse(
    // "success" 또는 "error"
    val status: String,

    // 생성된 캐릭터 이미지의 URL
    val character_url: String?
)
interface CharacterApiService {
    @FormUrlEncoded
    // 서버 (main.py): @app.post("/generate") -> 서버 함수 실행
    @POST("generate")
    suspend fun requestCharacterGeneration(
        @Field("user_id") userId: String,
        @Field("pet_image_url") imageUrl: String
    ): Response<CharacterGenerationResponse>
}