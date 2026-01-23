package com.android.guru2

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.android.guru2.data.SupabaseClientProvider
import com.bumptech.glide.Glide
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class HomeFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 레이아웃 인플레이트
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnRainbow = view.findViewById<ImageButton>(R.id.btn_rainbow)
        val ivCharacter = view.findViewById<ImageView>(R.id.iv_main_character)

        // 2. 현재 모드 상태 업데이트 (홈 모드)
        viewModel.isStarMode.value = false

        // 3. 무지개 버튼 클릭 리스너
        btnRainbow.setOnClickListener {
            showRainbowDialog()
        }

        // 4. 캐릭터 이미지 관찰 및 렌더링
        viewModel.characterUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {
                Glide.with(this)
                    .load(url)
                    .into(ivCharacter)
            }
        }

        // 5. 서버 데이터 로드
        viewModel.loadCharacter()
    }

    private fun showRainbowDialog() {
        // 커스텀 레이아웃 인플레이트
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rainbow_btn, null)

        val builder = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = builder.create()

        // 다이얼로그 배경을 투명하게 (둥근 모서리 적용을 위해 필수)
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 취소 버튼 클릭 시 다이얼로그만 닫기
        dialogView.findViewById<Button>(R.id.btn_alert_cancel).setOnClickListener {
            alertDialog.dismiss()
        }

        // 확인 버튼 클릭 시에만 StarFragment로 교체
        dialogView.findViewById<Button>(R.id.btn_alert_confirm).setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(StarFragment())
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}