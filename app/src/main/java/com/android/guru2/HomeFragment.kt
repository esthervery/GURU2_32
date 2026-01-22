package com.android.guru2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.guru2.data.SupabaseClientProvider
import com.bumptech.glide.Glide
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val btnRainbow = view.findViewById<ImageButton>(R.id.btn_rainbow)
        val ivCharacter = view.findViewById<ImageView>(R.id.iv_main_character)

        // 1. 무지개 버튼 클릭 시 StarFragment로 교체
        btnRainbow.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(StarFragment())
        }

        // 2. 캐릭터 이미지 로드
        loadCharacterImage(ivCharacter)

        return view
    }

    private fun loadCharacterImage(imageView: ImageView) {
        lifecycleScope.launch {
            try {
                // 현재 로그인 유저 ID 획득
                val currentUser = SupabaseClientProvider.client.auth.currentUserOrNull()
                val userId = currentUser?.id ?: return@launch

                // Supabase에서 캐릭터 URL 조회 (characterInfo 테이블)
                val characterData = SupabaseClientProvider.client.postgrest["characterInfo"]
                    .select {
                        filter {
                            eq("id", userId) // PK인 id 컬럼 사용
                        }
                    }.decodeSingle<CharacterDbModel>()

                // Glide를 사용하여 투명 배경 PNG 렌더링
                Glide.with(this@HomeFragment)
                    .load(characterData.character_url)
                    .into(imageView)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

// Supabase 응답 매핑 데이터 클래스
@Serializable
data class CharacterDbModel(
    val id: String,
    val character_url: String
)