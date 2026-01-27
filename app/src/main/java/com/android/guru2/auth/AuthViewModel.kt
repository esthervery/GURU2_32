package com.android.guru2.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.guru2.data.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginNavEvent {
    object ToMain : LoginNavEvent()
    object ToPetInfo : LoginNavEvent()
    data class Error(val message: String) : LoginNavEvent()
}

class AuthViewModel : ViewModel() {
    // 로딩 상태를 알리는 StateFlow
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _signUpSuccess = MutableStateFlow<Boolean?>(null)
    val signUpSuccess = _signUpSuccess.asStateFlow()

    // 이메일 인증 상태를 변경 (null: 확인 중, true: 로그인됨, false: 로그인 안 됨)
    private val _isAuthenticated = MutableStateFlow<Boolean?>(null)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    private val _loginEvent = MutableSharedFlow<LoginNavEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    init {
        // 앱 시작 시 세션 상태를 관찰하여 상태 업데이트
        viewModelScope.launch {
            SupabaseClientProvider.client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        _isAuthenticated.value = true
                        // 자동 로그인 성공 시에도 캐릭터 정보를 확인하여 화면 전환 시도
                        checkUserCharacterAndNavigate()
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _isAuthenticated.value = false
                    }
                    else -> {
                        _isAuthenticated.value = null
                    }
                }
            }
        }
    }

    // 캐릭터 정보 확인 로직을 별도 함수로 분리하여 자동/수동 로그인 모두에서 사용
    private fun checkUserCharacterAndNavigate() {
        viewModelScope.launch {
            try {
                // 현재 사용자 정보 가져오기
                val user = SupabaseClientProvider.client.auth.retrieveUserForCurrentSession()

                // 만약 유저 정보 자체가 없다면 로그인 안 된 상태로 처리 -> 세션 좀비 현상 해결
                if (user == null) {
                    _isAuthenticated.value = false
                    return@launch
                }

                // characterInfo 테이블 조회 (서버와 통신하며 세션 유효성 체크)
                val response = SupabaseClientProvider.client.postgrest["characterInfo"]
                    .select {
                        filter { eq("id", user.id) }
                    }
                val hasCharacterInfo = response.data != "[]"

                if (hasCharacterInfo) {
                    _loginEvent.emit(LoginNavEvent.ToMain)
                } else {
                    // 계정은 있는데 캐릭터만 없는 경우 (정상적인 신규 유저)
                    _loginEvent.emit(LoginNavEvent.ToPetInfo)
                }
            } catch (e: Exception){
                e.printStackTrace()
                try {
                    SupabaseClientProvider.client.auth.signOut()
                } catch (signOutError: Exception) {
                    // 이미 무효한 세션일 경우 signOut에서 에러 날 수 있으니 넘김
                }
                _isAuthenticated.value = false
                _loginEvent.emit(LoginNavEvent.Error("세션이 만료되었거나 유효하지 않은 계정입니다."))
            }
        }
    }

    fun signUp(emailInput: String, passwordInput: String) {
        // 이미 로딩 중이면 즉시 종료
        if (_isLoading.value) return

        viewModelScope.launch {
            // 로딩 시작 설정 먼저
            _isLoading.value = true

            try {
                SupabaseClientProvider.client.auth.signUpWith(
                    Email,
                    "app://confirm-signup") {
                    email = emailInput
                    password = passwordInput
                }
                _signUpSuccess.value = true
            } catch (e: Exception) {
                _signUpSuccess.value = false
                e.printStackTrace()
            } finally {
                // 성공하든 실패하든 마지막엔 반드시 로딩을 해제
                _isLoading.value = false
            }
        }
    }

    // 로그인 성공 후 공통 체크 함수를 호출
    fun signIn(emailInput: String, passwordInput: String) {
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.signInWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
                // 성공 시 캐릭터 정보 확인 프로세스로 진입
                checkUserCharacterAndNavigate()
            } catch (e: Exception) {
                _loginEvent.emit(LoginNavEvent.Error(e.localizedMessage ?: "로그인에 실패했습니다."))
                e.printStackTrace()
            }
        }
    }
}