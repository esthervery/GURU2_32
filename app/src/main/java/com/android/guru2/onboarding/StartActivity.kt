package com.android.guru2.onboarding

import android.R
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
import com.android.guru2.interaction.MainActivity
import com.android.guru2.auth.AuthViewModel
import com.android.guru2.auth.LoginFragment
import com.android.guru2.auth.LoginNavEvent
import com.android.guru2.auth.SignUpFragment
import kotlinx.coroutines.launch

class StartActivity : AppCompatActivity() {
    // 사용자의 로그인 상태를 체크하기 위해 ViewModel 선언
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // 검은 화면 방지: 레이아웃을 그리기 전 윈도우 배경을 흰색으로 선점
        window.setBackgroundDrawableResource(R.color.white)

        super.onCreate(savedInstanceState)
        setContentView(com.android.guru2.R.layout.activity_start)

        val btnStartEmail = findViewById<Button>(com.android.guru2.R.id.btn_start_email)
        val tvLoginLink = findViewById<TextView>(com.android.guru2.R.id.tv_login_link)
        val container = findViewById<FrameLayout>(com.android.guru2.R.id.fragment_container)

        // 인증 상태 관찰 (AuthViewModel의 null/true/false 상태 대응)
        lifecycleScope.launch {
            authViewModel.isAuthenticated.collect { authenticated ->
                when (authenticated) {
                    true -> {
                        // 로그인됨: 다음 화면으로 넘어갈 것이므로 버튼을 숨김
                        btnStartEmail.visibility = View.GONE
                        tvLoginLink.visibility = View.GONE
                    }
                    false -> {
                        // 로그인 안 됨: 사용자가 직접 로그인할 수 있게 버튼을 표시
                        btnStartEmail.visibility = View.VISIBLE
                        tvLoginLink.visibility = View.VISIBLE
                    }
                    null -> {
                        // 서버 확인 중: 버튼이 나타났다 사라지는 깜빡임을 막기 위해 숨김 유지
                        btnStartEmail.visibility = View.GONE
                        tvLoginLink.visibility = View.GONE
                    }
                }
            }
        }

        // 화면 전환 이벤트 관찰
        lifecycleScope.launch {
            authViewModel.loginEvent.collect { event ->
                when (event) {
                    is LoginNavEvent.ToMain -> navigateWithNoAnim(MainActivity::class.java)
                    is LoginNavEvent.ToPetInfo -> navigateWithNoAnim(PetInfoActivity::class.java)
                    is LoginNavEvent.Error -> {
                        // 에러 시 다시 버튼을 보여주어 재시도 가능하게 함
                        btnStartEmail.visibility = View.VISIBLE
                        tvLoginLink.visibility = View.VISIBLE
                    }
                }
            }
        }

        // 백스택 리스너: 프래그먼트가 닫힐 때 액티비티 UI를 완벽히 복구
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                // 버튼 다시 보이기
                btnStartEmail.visibility = View.VISIBLE
                tvLoginLink.visibility = View.VISIBLE
                // 가림막이었던 컨테이너를 투명하게 하고 아예 치워버림 (클릭 방해 금지)
                container.setBackgroundColor(Color.TRANSPARENT)
                container.visibility = View.GONE
            }
        }

        btnStartEmail.setOnClickListener { replaceFragment(SignUpFragment()) }
        tvLoginLink.setOnClickListener { replaceFragment(LoginFragment()) }

        // 최초 실행 시 딥링크 확인
        intent?.let { handleSupabaseDeeplink(it) }
    }

    // 화면 전환 시 시스템 애니메이션을 제거하여 검은 틈을 막는 전용 함수
    private fun <T> navigateWithNoAnim(cls: Class<T>) {
        val intent = Intent(this, cls)
        startActivity(intent)
        // 액티비티 전환 애니메이션 제거
        overridePendingTransition(0, 0)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // 앱이 실행 중인 상태에서 딥링크로 다시 진입한 경우 처리
        handleSupabaseDeeplink(intent)
    }

    // 딥링크 처리 로직
    private fun handleSupabaseDeeplink(intent: Intent) {
        val data: Uri? = intent.data
        if (data != null && data.scheme == "app" && data.host == "confirm-signup") {
            // Supabase SDK가 딥링크를 감시하여 자동으로 세션을 업데이트
            lifecycleScope.launch {
                try {
                    // SDK 버전 및 설정에 따라 자동 처리됨
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // 프래그먼트 교체를 위한 공통 함수
    fun replaceFragment(fragment: Fragment) {
        val container = findViewById<FrameLayout>(com.android.guru2.R.id.fragment_container)
        container.visibility = View.VISIBLE
        container.setBackgroundColor(Color.WHITE)

        // 프래그먼트가 뜰 때 액티비티 바닥에 깔린 버튼들을 숨김
        findViewById<Button>(com.android.guru2.R.id.btn_start_email).visibility = View.GONE
        findViewById<TextView>(com.android.guru2.R.id.tv_login_link).visibility = View.GONE

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(com.android.guru2.R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}