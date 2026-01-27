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
    // MainViewModel을 Activity 레벨에서 유지
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 초기 화면 설정 (홈)
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // 통합 바텀 네비게이션 (ComposeView 연결)
        val composeNavView = findViewById<ComposeView>(R.id.compose_bottom_nav)
        composeNavView.setContent {
            // 홈(1)이 기본
            var selectedItem by remember { mutableIntStateOf(1) }

            AppBottomNavigation(
                selectedItem = selectedItem,
                onItemSelected = { index ->
                    selectedItem = index
                    when (index) {
                        0 -> replaceFragment(CalendarComposeFragment())
                        1 -> {
                            // 상태 체크: StarMode가 true면 StarFragment를, 아니면 HomeFragment를 띄움
                            if (viewModel.isStarMode.value == true) {
                                replaceFragment(StarFragment())
                            } else {
                                replaceFragment(HomeFragment())
                            }
                        }
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

// 통합 바텀네비게이션 컴포저블
@Composable
fun AppBottomNavigation(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar(containerColor = Color.White, modifier = Modifier.height(70.dp)) {
        val items = listOf(
            Triple(0, R.drawable.ic_calendar, "캘린더"),
            Triple(1, R.drawable.ic_home, "홈"),
            Triple(2, R.drawable.ic_community, "커뮤니티")
        )
        items.forEach { (index, iconRes, label) ->
            NavigationBarItem(
                selected = selectedItem == index,
                onClick = { onItemSelected(index) },
                icon = { Icon(painter = painterResource(id = iconRes), contentDescription = label, tint = if (selectedItem == index) Color(0xFFF0724A) else Color.Gray, modifier = Modifier.size(24.dp)) }
            )
        }
    }
}

//캘린더 Compose 화면을 Fragment로 래핑
class CalendarComposeFragment : Fragment() {
    private val calendarViewModel: CalendarViewModel by viewModels()
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

// 커뮤니티 Compose 화면을 Fragment로 래핑
class CommunityComposeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CommunityScreen(onBackToCalendar = { (activity as? MainActivity)?.replaceFragment(CalendarComposeFragment()) })
            }
        }
    }
}