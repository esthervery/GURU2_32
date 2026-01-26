package com.android.guru2

import android.graphics.Color
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.android.guru2.data.SupabaseClientProvider
import com.bumptech.glide.Glide
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.resource.gif.GifDrawable

class HomeFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_home.xml 레이아웃 인플레이트
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivCharacter = view.findViewById<ImageView>(R.id.iv_main_character)
        val ivAnimation = view.findViewById<ImageView>(R.id.iv_pet_animation)
        val btnRainbow = view.findViewById<ImageButton>(R.id.btn_rainbow)
        val btnPlay = view.findViewById<ImageButton>(R.id.btn_action_play)

        // 캐릭터 데이터 관찰
        viewModel.characterUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {
                Glide.with(this).load(url).into(ivCharacter)
            }
        }
        viewModel.loadCharacter()

        // 재생 버튼 클릭 시 (WebP 애니메이션 실행)
        btnPlay.setOnClickListener {
            ivCharacter.visibility = View.VISIBLE
            ivAnimation.visibility = View.VISIBLE

            Glide.with(this)
                .load(R.drawable.ball_noblur) // .asGif() 삭제 -> WebP는 Gif 방식 사용 X
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        // 이미지가 애니메이션 가능한 형태(WebP/GIF)라면 실행
                        if (resource is android.graphics.drawable.Animatable) {
                            resource.start()

                            // 애니메이션 재생 시간만큼 기다렸다가 뷰를 숨깁니다.
                            // WebP는 GIF와 달리 Glide에서 1회 재생 제어가 까다롭기 때문에
                            // 애니메이션 길이(예: 2초 = 2000L) 후에 숨기는 방식이 가장 확실
                            ivAnimation.postDelayed({
                                ivAnimation.visibility = View.GONE
                            }, 2500L) // 공 애니메이션 실제 길이에 맞춰 숫자를 조절
                        }
                        return false
                    }
                })
                .into(ivAnimation)
        }

        btnRainbow.setOnClickListener { showRainbowDialog() }
    }

    private fun showRainbowDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rainbow_btn, null)
        val alertDialog = android.app.AlertDialog.Builder(requireContext()).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btn_alert_cancel).setOnClickListener { alertDialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btn_alert_confirm).setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(StarFragment())
            alertDialog.dismiss()
        }
        alertDialog.show()
    }
}