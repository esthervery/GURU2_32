package com.android.guru2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.android.guru2.ui.calendar.CalendarScreen //
import com.android.guru2.ui.calendar.CalendarViewModel
import com.android.guru2.ui.community.CommunityScreen //

class MainActivity : AppCompatActivity() {
    // 공용 뷰 모델 초기화
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. 초기 화면 설정 (홈)
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // 2. Compose로 구현된 통합 바텀 네비게이션 설정
        val composeNavView = findViewById<ComposeView>(R.id.compose_bottom_nav)
        composeNavView.setContent {
            // 선택된 아이템 상태 관리 (0: 캘린더, 1: 홈, 2: 커뮤니티)
            var selectedItem by remember { mutableIntStateOf(1) }

            AppBottomNavigation(
                selectedItem = selectedItem,
                onItemSelected = { index ->
                    selectedItem = index
                    when (index) {
                        0 -> replaceFragment(CalendarComposeFragment())
                        1 -> replaceFragment(HomeFragment())
                        2 -> replaceFragment(CommunityComposeFragment())
                    }
                }
            )
        }
    }

    // 프래그먼트 교체 함수
    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

/**
 * 팀원의 디자인을 일괄 적용한 바텀 네비게이션 컴포저블
 */
@Composable
fun AppBottomNavigation(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier.height(70.dp)
    ) {
        // 왼쪽: 캘린더
        NavigationBarItem(
            selected = selectedItem == 0,
            onClick = { onItemSelected(0) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = "캘린더",
                    tint = if (selectedItem == 0) Color(0xFFF0724A) else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        )
        // 중간: 홈 (HomeFragment/StarFragment 구역)
        NavigationBarItem(
            selected = selectedItem == 1,
            onClick = { onItemSelected(1) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "홈",
                    tint = if (selectedItem == 1) Color(0xFFF0724A) else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        )
        // 오른쪽: 커뮤니티
        NavigationBarItem(
            selected = selectedItem == 2,
            onClick = { onItemSelected(2) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_community),
                    contentDescription = "커뮤니티",
                    tint = if (selectedItem == 2) Color(0xFFF0724A) else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        )
    }
}

/**
 * 캘린더 Compose 화면을 Fragment로 래핑
 */
class CalendarComposeFragment : Fragment() {
    private val calendarViewModel: com.android.guru2.ui.calendar.CalendarViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CalendarScreen(
                    viewModel = calendarViewModel,
                    onNavigateToHome = { (activity as? MainActivity)?.replaceFragment(HomeFragment()) },
                    onNavigateToCommunity = { (activity as? MainActivity)?.replaceFragment(CommunityComposeFragment()) }
                )
            }
        }
    }
}

/**
 * 커뮤니티 Compose 화면을 Fragment로 래핑
 */
class CommunityComposeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CommunityScreen(
                    onBackToCalendar = { (activity as? MainActivity)?.replaceFragment(CalendarComposeFragment()) }
                )
            }
        }
    }
}