package com.android.guru2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.android.guru2.ui.calendar.CalendarScreen
import com.android.guru2.ui.community.CommunityScreen  // ✅ 이 줄 있는지 확인

class MainActivity : ComponentActivity() {

    private enum class Tab {
        CALENDAR,
        COMMUNITY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var tab by remember { mutableStateOf(Tab.CALENDAR) }

                    when (tab) {
                        Tab.CALENDAR -> {
                            CalendarScreen(
                                onNavigateToHome = { },
                                onNavigateToDiary = {
                                    tab = Tab.COMMUNITY  // ✅ 커뮤니티로 전환
                                }
                            )
                        }

                        Tab.COMMUNITY -> {
                            CommunityScreen(  // ✅ 이 부분 있는지 확인
                                onBackToCalendar = { tab = Tab.CALENDAR }
                            )
                        }
                    }
                }
            }
        }
    }
}