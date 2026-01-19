package com.android.guru2.ui.auth.com.android.guru2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.guru2.data.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    // 회원가입 상태를 관리 (null: 대기, true: 성공, false: 실패)
    private val _signUpSuccess = MutableStateFlow<Boolean?>(null)
    val signUpSuccess = _signUpSuccess.asStateFlow()

    fun signUp(emailInput: String, passwordInput: String) {
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.signUpWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
                _signUpSuccess.value = true // 성공 시 true로 변경
            } catch (e: Exception) {
                _signUpSuccess.value = false // 실패 시 false로 변경
                e.printStackTrace()
            }
        }
    }

    // 로그인 함수
    fun signIn(emailInput: String, passwordInput: String) {
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.signInWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
                // 성공 시 처리
            } catch (e: Exception) {
                // 에러 발생 시 처리
            }
        }
    }
}