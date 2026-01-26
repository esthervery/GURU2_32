package com.android.guru2.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://mawgilzjqqklmqqnhzur.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1hd2dpbHpqcXFrbG1xcW5oenVyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njg1NDYwNTYsImV4cCI6MjA4NDEyMjA1Nn0.gkAhRgCs2wkXsye10ASKlrIkYvLBLn36gCnrmf4jut8"
    ) {
        install(Auth)
        install(Postgrest)
    }
}