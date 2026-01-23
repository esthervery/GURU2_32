package com.android.guru2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.android.guru2.ui.calendar.CalendarScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalendarScreen(
                        onNavigateToHome = {
                            // TODO: 홈 화면으로 이동
                        },
                        onNavigateToDiary = {
                            // TODO: 일기 목록 화면으로 이동
                        }
                    )
                }
            }
        }
    }
}