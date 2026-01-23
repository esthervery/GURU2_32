package com.android.guru2

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    // 1. 뷰 모델 초기화 (by viewModels 사용을 위해 fragment-ktx 의존성 필요)
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 2. 반드시 setContentView 호출 후에 뷰를 찾아야 합니다.
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // 초기 화면 설정
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // 3. 바텀 네비게이션 클릭 리스너 설정
//        bottomNav.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//
//            }
//        }
    }
    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}