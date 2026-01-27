package com.android.guru2.data

import com.android.guru2.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.Json

object SupabaseClientProvider {
    // lateinit을 사용하여 초기화 시점을 제어
    lateinit var client: io.github.jan.supabase.SupabaseClient

    fun init() {
        // build.gradle.kts에서 설정한 BuildConfig 값을 사용
        client = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
            // install(Realtime)
        }
    }
}