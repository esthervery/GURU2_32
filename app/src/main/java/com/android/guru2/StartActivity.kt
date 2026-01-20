package com.android.guru2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val btnStartEmail = findViewById<Button>(R.id.btn_start_email)
        val tvLoginLink = findViewById<TextView>(R.id.tv_login_link)

        // "이메일로 시작하기" 클릭 시 회원가입 프래그먼트로 이동
        btnStartEmail.setOnClickListener {
            replaceFragment(SignUpFragment())
        }

        // "로그인" 클릭 시 로그인 프래그먼트로 이동
        tvLoginLink.setOnClickListener {
            replaceFragment(LoginFragment())
        }

        intent?.let { handleSupabaseDeeplink(it) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // 앱이 실행 중인 상태에서 딥링크로 다시 진입한 경우 처리
        handleSupabaseDeeplink(intent)
    }

    private fun handleSupabaseDeeplink(intent: Intent) {
        val data: Uri? = intent.data
        if (data != null && data.scheme == "app" && data.host == "confirm-signup") {
            // Supabase SDK가 딥링크를 처리하도록 전달
            // 이 과정이 성공하면 AuthViewModel의 sessionStatus가 Authenticated로 변합니다
            lifecycleScope.launch {
                try {
                    // supabase-kt의 자동 딥링크 처리 로직 (버전에 따라 상이할 수 있음)
                    // 별도의 처리 없이도 SDK가 intent를 감시하지만, 명시적으로 확인이 필요한 경우 사용
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // 프래그먼트 교체를 위한 공통 함수
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // 뒤로가기 버튼을 누르면 이전 화면으로 돌아옴
            .commit()
    }
}