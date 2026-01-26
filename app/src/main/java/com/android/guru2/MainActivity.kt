package com.android.guru2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.android.guru2.ui.theme.Guru2Theme
import com.android.guru2.ui.calendar.CalendarScreen
import com.android.guru2.ui.community.CommunityScreen
// 김에스더 코드에서 import
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    // 뷰 모델 초기화 (by viewModels 사용을 위해 fragment-ktx 의존성 필요)
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // 기존 xml 유지 

        // 반드시 setContentView 호출 후에 BottomNavigationView 찾기
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // 초기 화면 설정
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }
    
        // 바텀 네비게이션 클릭 리스너
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_calendar -> {
                    replaceFragment(CalendarFragment())
                    true
                }
                R.id.nav_community -> {
                    // Compose 화면을 Fragment로 래핑
                    replaceFragment(CommunityComposeFragment())
                    true
                }
                else -> false
            }
        }
    }
    
    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

// Compose 화면을 Fragment로 래핑 
class CommunityComposeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                // Guru2Theme { // 테마 있으면 
                    CommunityScreen(
                        onBackToCalendar = {
                            (activity as? MainActivity)?.replaceFragment(CalendarFragment())
                        }
                    )
                // }
            }
        }
    }
}