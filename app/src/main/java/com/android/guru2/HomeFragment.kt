package com.android.guru2

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

class HomeFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    // 연타 방지 및 상태 관리 변수
    private var isAnimating = false
    private var hideRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivCharacter = view.findViewById<ImageView>(R.id.iv_main_character)
        val ivAnimation = view.findViewById<ImageView>(R.id.iv_pet_animation)
        val ivBubble = view.findViewById<ImageView>(R.id.iv_speech_bubble)
        val btnRainbow = view.findViewById<ImageButton>(R.id.btn_rainbow)

        val btnPlay = view.findViewById<ImageButton>(R.id.btn_action_play)
        val btnFeed = view.findViewById<ImageButton>(R.id.btn_action_feed)
        val btnWash = view.findViewById<ImageButton>(R.id.btn_action_wash)

        // 캐릭터 데이터 로드 및 관찰
        viewModel.characterUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {
                Glide.with(this).load(url).into(ivCharacter)
            }
        }
        viewModel.loadCharacter()

        btnRainbow.setOnClickListener { showRainbowDialog() }

        // 각 버튼 클릭 시 전용 애니메이션과 전용 말풍선 이미지 지정
        btnPlay.setOnClickListener {
            startInteraction(ivAnimation, ivBubble, R.drawable.ball_animate, R.drawable.bubble_play_img, 2500L)
        }

        btnFeed.setOnClickListener {
            startInteraction(ivAnimation, ivBubble, R.drawable.meal_animate, R.drawable.bubble_feed_img, 2500L)
        }

        btnWash.setOnClickListener {
            startInteraction(ivAnimation, ivBubble, R.drawable.hand_animate, R.drawable.bubble_wash_img, 2500L)
        }
    }

    // 연타 방지 로직이 포함된 인터랙션 실행 함수
    private fun startInteraction(animView: ImageView, bubbleView: ImageView, animRes: Int, bubbleRes: Int, duration: Long) {
        // 이미 진행 중이면 클릭 무시
        if (isAnimating) return
        // 진행 중 설정
        isAnimating = true
        // 버튼 잠금
        setButtonsEnabled(false)

        // 이전 예약된 작업 취소 및 Glide 초기화
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
                        resource.start()

                        hideRunnable = Runnable {
                            animView.visibility = View.GONE
                            // 애니메이션 종료 후 말풍선 페이드 효과 시작
                            showSpeechBubbleEffect(bubbleView, bubbleRes)
                        }
                        animView.postDelayed(hideRunnable!!, duration)
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

    // 말풍선 페이드 인(0.3초) -> 유지(1초) -> 페이드 아웃(0.3초) 설정
    private fun showSpeechBubbleEffect(bubbleView: ImageView, imageRes: Int) {
        bubbleView.setImageResource(imageRes)
        bubbleView.visibility = View.VISIBLE

        // 페이드 인
        bubbleView.animate().alpha(1f).setDuration(300).withEndAction {
            // 1초 대기 후 페이드 아웃
            bubbleView.postDelayed({
                bubbleView.animate().alpha(0f).setDuration(300).withEndAction {
                    bubbleView.visibility = View.GONE
                    // 모든 과정 완료 후 연타 방지 해제
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

    private fun showRainbowDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rainbow_btn, null)
        val alertDialog = android.app.AlertDialog.Builder(requireContext()).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogView.findViewById<Button>(R.id.btn_alert_cancel).setOnClickListener { alertDialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btn_alert_confirm).setOnClickListener {
            viewModel.isStarMode.value = true
            (activity as? MainActivity)?.replaceFragment(StarFragment())
            alertDialog.dismiss()
        }
        alertDialog.show()
    }
}