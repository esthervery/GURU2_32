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
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class StarFragment : Fragment() {
    // MainActivity와 공유하는 뷰 모델
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_star.xml 레이아웃 인플레이트
        return inflater.inflate(R.layout.fragment_star, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰 연결
        val ivStarCharacter = view.findViewById<ImageView>(R.id.iv_star_character)
        val ivStarAnimation = view.findViewById<ImageView>(R.id.iv_star_animation)

        val btnPlay = view.findViewById<ImageButton>(R.id.btn_action_play)
        val btnFeed = view.findViewById<ImageButton>(R.id.btn_action_feed)
        val btnWash = view.findViewById<ImageButton>(R.id.btn_action_wash)

        // 캐릭터 데이터 관찰 (HomeFragment와 동일)
        viewModel.characterUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {
                Glide.with(this).load(url).into(ivStarCharacter)
            }
        }

        // 버튼 리스너 설정 (놀아주기 - ball_noblur 적용)
        btnPlay.setOnClickListener {
            playAnimation(ivStarCharacter, ivStarAnimation, R.drawable.ball_noblur, 2500L)
        }

        // 버튼 리스너 설정 (밥주기 - 예시 파일명, 실제 파일명으로 바꾸세요)
//        btnFeed.setOnClickListener {
//            // R.drawable.feed_animation 등으로 변경해서 쓰시면 됩니다.
//            playAnimation(ivStarCharacter, ivStarAnimation, R.drawable.ball_noblur, 3000L)
//        }

        // 5. 버튼 리스너 설정 (쓰다듬기 - 예시 파일명)
//        btnWash.setOnClickListener {
//            playAnimation(ivStarCharacter, ivStarAnimation, R.drawable.ball_noblur, 3000L)
//        }
    }

    //애니메이션을 재생하고 일정 시간 후 숨기는 공통 함수
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