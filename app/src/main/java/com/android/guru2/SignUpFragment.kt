package com.android.guru2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.android.guru2.ui.auth.com.android.guru2.AuthViewModel
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    // ViewModel 연결 (라이브러리 추가가 선행되어야 함)
    private val authViewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // findViewById로 UI 요소 가져오기
        val emailEdit = view.findViewById<EditText>(R.id.editSignupEmail)
        val passwordEdit = view.findViewById<EditText>(R.id.editSignupPassword)
        val signUpButton = view.findViewById<Button>(R.id.btnSignup)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)


        // 뒤로 가기 버튼 로직
        btnBack.setOnClickListener {
            // StartActivity에서 addToBackStack을 사용했으므로 back stack을 팝(pop)
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.signUpSuccess.collectLatest { success ->
                when (success) {
                    true -> Toast.makeText(requireContext(), "인증 이메일이 발송되었습니다. 메일함을 확인해주세요.", Toast.LENGTH_LONG).show()
                    false -> Toast.makeText(requireContext(), "회원가입에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    null -> { /* 대기 상태 */ }
                }
            }
        }

        signUpButton.setOnClickListener {
            val email = emailEdit.text.toString()
            val password = passwordEdit.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.signUp(email, password)
            } else {
                Toast.makeText(requireContext(), "정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}