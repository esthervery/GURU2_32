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

class HomeFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var mediaPlayer: MediaPlayer? = null

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

        val tvActionVideo = view.findViewById<TextureView>(R.id.tv_action_video)

        // 버튼 리스너 연결 (TextureView 전달)
        view.findViewById<ImageButton>(R.id.btn_action_play).setOnClickListener {
            playActionVideo(R.raw.action_play, tvActionVideo)
        }

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
    private fun playActionVideo(videoResId: Int, textureView: TextureView) {
        // 1. 기존 재생 중인 MediaPlayer 해제
        mediaPlayer?.release()
        mediaPlayer = null

        textureView.visibility = View.VISIBLE

        // 2. SurfaceTexture 준비 상태 확인
        val surfaceTexture = textureView.surfaceTexture
        if (surfaceTexture != null) {
            // 이미 준비된 경우 바로 재생 시작
            startMediaPlayer(videoResId, surfaceTexture)
        } else {
            // 아직 준비되지 않은 경우 리스너를 등록하여 기다림
            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
                    startMediaPlayer(videoResId, st)
                }
                override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {}
                override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean = true
                override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
            }
        }
    }

    private fun startMediaPlayer(videoResId: Int, surfaceTexture: SurfaceTexture) {
        try {
            mediaPlayer = MediaPlayer().apply {
                val assetFileDescriptor = resources.openRawResourceFd(videoResId)
                setDataSource(assetFileDescriptor.fileDescriptor, assetFileDescriptor.startOffset, assetFileDescriptor.length)
                assetFileDescriptor.close()

                // 여기서 생성한 Surface를 사용
                setSurface(android.view.Surface(surfaceTexture))

                prepareAsync()
                setOnPreparedListener { mp ->
                    // 👈 영상 비율에 맞춰 TextureView 크기 조절 함수 호출
                    adjustAspectRatio(view?.findViewById(R.id.tv_action_video)!!, mp.videoWidth, mp.videoHeight)
                    start()
                }
                setOnCompletionListener {
                    // 재생 완료 시 처리
                    val textureView = (view?.findViewById<TextureView>(R.id.tv_action_video))
                    textureView?.visibility = View.GONE
                    release()
                    mediaPlayer = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun adjustAspectRatio(textureView: TextureView, videoWidth: Int, videoHeight: Int) {
        val viewWidth = textureView.width
        val viewHeight = textureView.height
        val aspectRatio = videoHeight.toDouble() / videoWidth

        val newWidth: Int
        val newHeight: Int
        if (viewHeight > (viewWidth * aspectRatio).toInt()) {
            newWidth = viewWidth
            newHeight = (viewWidth * aspectRatio).toInt()
        } else {
            newWidth = (viewHeight / aspectRatio).toInt()
            newHeight = viewHeight
        }

        val xOff = (viewWidth - newWidth) / 2f
        val yOff = (viewHeight - newHeight) / 2f

        val matrix = android.graphics.Matrix()
        textureView.getTransform(matrix)
        matrix.setScale(newWidth.toFloat() / viewWidth, newHeight.toFloat() / viewHeight)
        matrix.postTranslate(xOff, yOff)
        textureView.setTransform(matrix)
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

    // 프래그먼트 파괴 시 리소스 해제
    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}