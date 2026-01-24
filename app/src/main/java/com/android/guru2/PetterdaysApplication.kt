package com.android.guru2

import android.app.Application
import com.android.guru2.data.SupabaseClientProvider

class PetterdaysApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 앱이 켜지자마자 가장 먼저 Supabase를 초기화합니다.
        // SupabaseClientProvider 내부의 초기화 로직이 여기서 실행됩니다.
        SupabaseClientProvider.init()
    }
}