package com.android.guru2.onboarding

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.VideoView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.android.guru2.R

class LoadingFragment : Fragment(R.layout.fragment_loading) {

    private var videoView: VideoView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뒤로가기 무시
            }
        })

        // 비디오 뷰 설정
        videoView = view.findViewById(R.id.loading_bg)

        // res/raw 폴더에 loading_bg.mp4가 존재해야 함
        val videoPath = "android.resource://${requireContext().packageName}/${R.raw.loading_bg}"
        val uri = Uri.parse(videoPath)

        videoView?.setVideoURI(uri)

        videoView?.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = true
            mediaPlayer.setVolume(0f, 0f)

            // 영상의 원래 비율을 유지하며 화면을 꽉 채우는 로직 (Center Crop 방식)
            val videoWidth = mediaPlayer.videoWidth.toFloat()
            val videoHeight = mediaPlayer.videoHeight.toFloat()
            val viewWidth = videoView!!.width.toFloat()
            val viewHeight = videoView!!.height.toFloat()

            val xScale = viewWidth / videoWidth
            val yScale = viewHeight / videoHeight
            val scale = Math.max(xScale, yScale)

            videoView!!.scaleX = scale / xScale
            videoView!!.scaleY = scale / yScale

            videoView?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        videoView?.pause()
    }

    override fun onResume() {
        super.onResume()
        videoView?.start()
    }
}