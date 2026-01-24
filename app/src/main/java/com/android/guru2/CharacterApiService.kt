package com.android.guru2

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

// CharacterApiService.kt 파일 수정
data class CharacterGenerationResponse(
    val status: String,        // "success" 또는 "error"
    val character_url: String? // 생성된 캐릭터 이미지의 URL (서버의 키값과 일치해야 함)
)
interface CharacterApiService {
    @FormUrlEncoded
    // 서버 (main.py): @app.post("/generate") -> 서버 handle_generate_request 함수 실행
    @POST("generate")
    suspend fun requestCharacterGeneration(
        @Field("user_id") userId: String,
        @Field("pet_image_url") imageUrl: String
    ): Response<CharacterGenerationResponse>
}