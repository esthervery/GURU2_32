package com.android.guru2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.guru2.data.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus // 명시적 임포트
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    // 회원가입 상태를 관리 (null: 대기, true: 성공, false: 실패)
    private val _signUpSuccess = MutableStateFlow<Boolean?>(null)
    val signUpSuccess = _signUpSuccess.asStateFlow()

    // 사용자 인증 여부 상태 추가
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    init {
        viewModelScope.launch {
            // status의 타입을 명시적으로 확인하여 컴파일러 오류 방지
            SupabaseClientProvider.client.auth.sessionStatus.collect { status: SessionStatus ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        _isAuthenticated.value = true
                    }
                    else -> {
                        _isAuthenticated.value = false
                    }
                }
            }
        }
    }

    fun signUp(emailInput: String, passwordInput: String) {
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.signUpWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
                _signUpSuccess.value = true
            } catch (e: Exception) {
                _signUpSuccess.value = false
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