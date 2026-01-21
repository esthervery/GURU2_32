package com.android.guru2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.guru2.data.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus // 명시적 임포트
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

    private val _loginEvent = MutableSharedFlow<LoginNavEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    // 로그인 함수
    fun signIn(emailInput: String, passwordInput: String) {
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.signInWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
                // 성공 시 처리
                val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id
                    ?: throw Exception("사용자 정보를 찾을 수 없습니다.")

                // 3. petInfo 테이블에 해당 ID의 데이터가 있는지 확인
                val hasCharacterInfo = SupabaseClientProvider.client.postgrest["characterInfo"]
                    .select {
                        filter {
                            eq("id", userId) // auth.users.id와 연결된 id 컬럼 검사
                        }
                    }.data != "[]" // 데이터가 비어있지 않으면 캐릭터 정보가 존재하는 것

                // 4. 결과에 따라 이벤트 전송
                if (hasCharacterInfo) {
                    // 캐릭터가 이미 생성되어 있다면 메인 화면으로
                    _loginEvent.emit(LoginNavEvent.ToMain)
                } else {
                    // 캐릭터 정보가 없다면 반려동물 정보 등록 화면으로
                    _loginEvent.emit(LoginNavEvent.ToPetInfo)
                }
            } catch (e: Exception) {
                // 에러 발생 시 처리 로직
                _loginEvent.emit(LoginNavEvent.Error(e.localizedMessage ?: "로그인에 실패했습니다."))
                e.printStackTrace()
            }
        }
    }
}