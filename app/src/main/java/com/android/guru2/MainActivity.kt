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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Guru2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    var currentTab by remember { mutableStateOf(Tab.CALENDAR) }

    when (currentTab) {
        Tab.CALENDAR -> {
            CalendarScreen(
                onNavigateToHome = { currentTab = Tab.HOME },
                onNavigateToCommunity = { currentTab = Tab.COMMUNITY }
            )
        }
        Tab.HOME -> {
            CalendarScreen(
                onNavigateToHome = { },
                onNavigateToCommunity = { currentTab = Tab.COMMUNITY }
            )
        }
        Tab.COMMUNITY -> {
            CommunityScreen(
                onBackToCalendar = { currentTab = Tab.CALENDAR }
            )
        }
    }
}

enum class Tab {
    CALENDAR,
    HOME,
    COMMUNITY
}