package com.android.guru2

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class StarFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    // 연타 방지 및 타이머 관리 변수 추가
    private var isAnimating = false
    private var hideRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_star, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivCharacter = view.findViewById<ImageView>(R.id.iv_main_character)
        val ivAnimation = view.findViewById<ImageView>(R.id.iv_pet_animation)
        val ivBubble = view.findViewById<ImageView>(R.id.iv_speech_bubble)

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

        // 각 버튼별 전용 애니메이션과 전용 말풍선 이미지 설정
        btnPlay.setOnClickListener {
            startStarInteraction(ivAnimation, ivBubble, R.drawable.ball_animate, R.drawable.bubble_play_img)
        }

        btnFeed.setOnClickListener {
            startStarInteraction(ivAnimation, ivBubble, R.drawable.meal_animate, R.drawable.bubble_feed_img)
        }

        btnWash.setOnClickListener {
            startStarInteraction(ivAnimation, ivBubble, R.drawable.hand_animate, R.drawable.bubble_wash_img)
        }
    }

    // 연타 방지가 포함된 애니메이션 실행 함수
    private fun startStarInteraction(animView: ImageView, bubbleView: ImageView, animRes: Int, bubbleRes: Int) {
        if (isAnimating) return
        isAnimating = true
        setButtonsEnabled(false) // 버튼 잠금

        hideRunnable?.let { animView.removeCallbacks(it) }
        Glide.with(this).clear(animView)
        animView.visibility = View.VISIBLE

        Glide.with(this)
            .asDrawable()
            .load(animRes)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .listener(object : RequestListener<Drawable> {
                override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    if (resource is Animatable) {
                        resource.stop()
                        resource.start() // 첫 프레임부터 재생 강제

                        hideRunnable = Runnable {
                            animView.visibility = View.GONE
                            // ✅ 애니메이션 종료 후 말풍선 페이드 효과 실행
                            showSpeechBubble(bubbleView, bubbleRes)
                        }
                        animView.postDelayed(hideRunnable!!, 2500L) // 애니메이션 재생 시간
                    }
                    return false
                }
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                    isAnimating = false
                    setButtonsEnabled(true)
                    return false
                }
            })
            .into(animView)
    }

    // 말풍선 페이드 인 -> 1초 유지 -> 페이드 아웃 로직
    private fun showSpeechBubble(bubbleView: ImageView, imageRes: Int) {
        bubbleView.setImageResource(imageRes)
        bubbleView.visibility = View.VISIBLE

        // 페이드 인 (0.3초)
        bubbleView.animate().alpha(1f).setDuration(300).withEndAction {
            // 1초 대기
            bubbleView.postDelayed({
                // 페이드 아웃 (0.3초)
                bubbleView.animate().alpha(0f).setDuration(300).withEndAction {
                    bubbleView.visibility = View.GONE
                    // 모든 시퀀스가 끝나면 연타 방지 해제
                    isAnimating = false
                    setButtonsEnabled(true)
                }.start()
            }, 1000)
        }.start()
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        view?.let {
            it.findViewById<ImageButton>(R.id.btn_action_play).isEnabled = enabled
            it.findViewById<ImageButton>(R.id.btn_action_feed).isEnabled = enabled
            it.findViewById<ImageButton>(R.id.btn_action_wash).isEnabled = enabled
        }
    }
}