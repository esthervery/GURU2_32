package com.android.guru2

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class StartActivity : AppCompatActivity() {
    // 사용자의 로그인 상태를 체크하기 위해 ViewModel 선언
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val btnStartEmail = findViewById<Button>(R.id.btn_start_email)
        val tvLoginLink = findViewById<TextView>(R.id.tv_login_link)
        val container = findViewById<FrameLayout>(R.id.fragment_container)

        // 자동 로그인 로직: 세션 상태 관찰
        lifecycleScope.launch {
            authViewModel.loginEvent.collect { event ->
                when (event) {
                    is LoginNavEvent.ToMain -> {
                        // 이미 로그인된 세션이 있다면 메인으로 바로 이동
                        val intent = Intent(this@StartActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    is LoginNavEvent.ToPetInfo -> {
                        // 정보가 없으면 등록 화면으로 이동
                        val intent = Intent(this@StartActivity, PetInfoActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else -> {
                        // 세션이 없으면 현재 화면(StartActivity) 유지
                    }
                }
            }
        }

        // 프래그먼트가 popBackStack() 되어 사라지면 이 리스너가 호출
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                // 1. 숨겼던 버튼들을 다시 보이게 함
                btnStartEmail.visibility = View.VISIBLE
                tvLoginLink.visibility = View.VISIBLE
                // 2. 가림막으로 썼던 하얀 배경을 다시 투명하게 만듦
                container.setBackgroundColor(Color.TRANSPARENT)
            }
        }

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
    fun replaceFragment(fragment: Fragment) {
        val container = findViewById<FrameLayout>(R.id.fragment_container)
        container.setBackgroundColor(android.graphics.Color.WHITE)
        // 프래그먼트가 뜰 때 액티비티 바닥에 깔린 버튼들을 숨김
        findViewById<Button>(R.id.btn_start_email).visibility = View.GONE
        findViewById<TextView>(R.id.tv_login_link).visibility = View.GONE

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}