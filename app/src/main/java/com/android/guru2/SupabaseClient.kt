package com.android.guru2.data

// 빌드컨피그는 앱의 namespace에 생성
import com.android.guru2.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.Json

object SupabaseClientProvider {
    val client by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            // JSON 설정: 서버의 필드가 클래스보다 많아도 에러가 나지 않게 설정
            defaultSerializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
            install(Auth) {
                // Auth 설정 옵션 (선택사항)
            }

            // 데이터베이스 쿼리 (필터링, CRUD 등 - neq 사용 시 필수)
            install(Postgrest)

            // 실시간 데이터 변경 감지 (필요 시)
            install(Realtime)

            // 스토리지 설치
            install(Storage)
        }
    }

    // 공식 문서 사용 예시
//    val supabase = createSupabaseClient(supabaseUrl, supabaseKey) {
//        //Already the default serializer, but you can provide a custom Json instance (optional):
//        defaultSerializer = KotlinXSerializer(Json {
//            //apply your custom config
//        })
//    }
}