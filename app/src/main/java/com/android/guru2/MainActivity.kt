package com.android.guru2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.android.guru2.ui.calendar.CalendarScreen
import com.android.guru2.ui.calendar.CalendarViewModel
import com.android.guru2.ui.community.CommunityScreen
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_calendar -> {
                    // 캘린더 화면으로 이동
                    replaceFragment(CalendarComposeFragment())
                    true
                }
                R.id.nav_community -> {
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


// 캘린더 Compose 화면을 Fragment로 래핑
class CalendarComposeFragment : Fragment() {
    // 팀원의 캘린더 뷰모델 연결
    private val calendarViewModel: CalendarViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                // CalendarScreen에 필요한 모든 인자 채우기
                CalendarScreen(
                    viewModel = calendarViewModel,
                    onNavigateToHome = {
                        (activity as? MainActivity)?.replaceFragment(HomeFragment())
                    },
                    onNavigateToCommunity = {
                        (activity as? MainActivity)?.replaceFragment(CommunityComposeFragment())
                    }
                )
            }
        }
    }
}

// 커뮤니티 Compose 화면을 Fragment로 래핑
class CommunityComposeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CommunityScreen(
                    onBackToCalendar = {
                        (activity as? MainActivity)?.replaceFragment(CalendarComposeFragment())
                    }
                )
            }
        }
    }
}