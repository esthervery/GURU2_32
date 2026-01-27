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

class StarFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    // 연타 방지 및 타이머 관리 변수
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

        val btnHome = view.findViewById<ImageButton>(R.id.btn_home)

        // 캐릭터 데이터 관찰
        viewModel.characterUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {
                Glide.with(this).load(url).into(ivCharacter)
            }
        }
        viewModel.loadCharacter()

        // 홈 버튼 클릭 시 다이얼로그 표시
        btnHome.setOnClickListener {
            showHomeDialog()
        }

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

    // 홈 복귀 확인 다이얼로그 표시 함수
    private fun showHomeDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_home_btn, null)
        val alertDialog = android.app.AlertDialog.Builder(requireContext()).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btn_alert_cancel).setOnClickListener {
            alertDialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_alert_confirm).setOnClickListener {
            // StarMode 해제 및 HomeFragment로 교체
            viewModel.isStarMode.value = false
            (activity as? MainActivity)?.replaceFragment(HomeFragment())
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    // 애니메이션 실행 함수 (기존과 동일)
    private fun startStarInteraction(animView: ImageView, bubbleView: ImageView, animRes: Int, bubbleRes: Int) {
        if (isAnimating) return
        isAnimating = true
        setButtonsEnabled(false)

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
                            showSpeechBubble(bubbleView, bubbleRes)
                        }
                        animView.postDelayed(hideRunnable!!, 2500L)
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

    private fun showSpeechBubble(bubbleView: ImageView, imageRes: Int) {
        bubbleView.setImageResource(imageRes)
        bubbleView.visibility = View.VISIBLE

        bubbleView.animate().alpha(1f).setDuration(300).withEndAction {
            bubbleView.postDelayed({
                bubbleView.animate().alpha(0f).setDuration(300).withEndAction {
                    bubbleView.visibility = View.GONE
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