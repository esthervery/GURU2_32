package com.android.guru2.data

// 빌드컨피그는 앱의 namespace에 생성
import com.android.guru2.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClientProvider {
    val client by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                // Auth 설정 옵션 (선택사항)
            }

            // 데이터베이스 쿼리 (필터링, CRUD 등 - neq 사용 시 필수)
            install(Postgrest)

            // 실시간 데이터 변경 감지 (필요 시)
            install(Realtime)
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