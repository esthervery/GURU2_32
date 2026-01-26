package com.android.guru2

import android.graphics.Color
import android.graphics.SurfaceTexture
import android.graphics.drawable.Animatable
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
        val btnFeed = view.findViewById<ImageButton>(R.id.btn_action_feed)
        val btnWash = view.findViewById<ImageButton>(R.id.btn_action_wash)

        // 캐릭터 데이터 관찰
        viewModel.characterUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {
                Glide.with(this).load(url).into(ivCharacter)
            }
        }
        viewModel.loadCharacter()

        // 무지개 버튼 클릭 리스너
        btnRainbow.setOnClickListener { showRainbowDialog() }

        // 재생 버튼 클릭 시 (WebP 애니메이션 실행)
        btnPlay.setOnClickListener {
            playAnimation(ivCharacter, ivAnimation, R.drawable.ball_animate, 2500L)
        }

        btnFeed.setOnClickListener {
            // R.drawable.feed_animation 등으로 변경해서 쓰시면 됩니다.
            playAnimation(ivCharacter, ivAnimation, R.drawable.meal_animate, 2500L)
        }

        btnWash.setOnClickListener {
            playAnimation(ivCharacter, ivAnimation, R.drawable.hand_animate, 2500L)
        }
    }

    private fun showRainbowDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rainbow_btn, null)
        val alertDialog = android.app.AlertDialog.Builder(requireContext()).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btn_alert_cancel).setOnClickListener { alertDialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btn_alert_confirm).setOnClickListener {
            // 뷰모델에 '강아지가 무지개 다리를 건넜음' 확정 기록
            viewModel.isStarMode.value = true
            (activity as? MainActivity)?.replaceFragment(StarFragment())
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun playAnimation(character: ImageView, animationView: ImageView, drawableRes: Int, duration: Long) {
        // 강아지 캐릭터는 유지
        character.visibility = View.VISIBLE
        animationView.visibility = View.VISIBLE

        Glide.with(this)
            .load(drawableRes)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean
                ): Boolean = false

                override fun onResourceReady(
                    resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean
                ): Boolean {
                    // 애니메이션(WebP)이면 재생 시작
                    if (resource is Animatable) {
                        resource.start()

                        // 실제 애니메이션 길이만큼 기다렸다가 뷰 숨기기
                        animationView.postDelayed({
                            animationView.visibility = View.GONE
                        }, duration)
                    }
                    return false
                }
            })
            .into(animationView)
    }
}