package com.android.guru2

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

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
    }

    // 프래그먼트 교체를 위한 공통 함수
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // 뒤로가기 버튼을 누르면 이전 화면으로 돌아옴
            .commit()
    }
}