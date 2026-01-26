package com.android.guru2

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.VideoView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

class LoadingFragment : Fragment(R.layout.fragment_loading) {

    private var videoView: VideoView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 휴대폰 자체 '뒤로가기' 버튼 무효화
        // 사용자가 물리 버튼이나 제스처로 뒤로 가려 해도 아무 반응 없도록 만듦
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
        // 3. 비디오 준비 완료 시 재생 (무한 반복, 소리 없음)
//        videoView?.setOnPreparedListener { mediaPlayer ->
//            mediaPlayer.isLooping = true
//            mediaPlayer.setVolume(0f, 0f) // 무음 설정
//
//            // 영상 비율에 맞춰 꽉 채우기 (Scale 조절)
//            val videoRatio = mediaPlayer.videoWidth / mediaPlayer.videoHeight.toFloat()
//            val screenRatio = videoView!!.width / videoView!!.height.toFloat()
//            val scale = videoRatio / screenRatio
//            if (scale >= 1f) videoView!!.scaleX = scale else videoView!!.scaleY = 1f / scale
//
//            videoView?.start()
//        }
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