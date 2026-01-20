package com.android.guru2

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.android.guru2.AuthViewModel

class LoginFragment : Fragment(R.layout.fragment_login) {

    // ViewModel 선언
    private val authViewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // findViewById로 UI 요소 찾기
        val emailEdit = view.findViewById<EditText>(R.id.editEmail)
        val passwordEdit = view.findViewById<EditText>(R.id.editPassword)
        val loginButton = view.findViewById<Button>(R.id.btnLogin)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

        // 회원가입에서 왔는지 확인
        val fromSignUp = arguments?.getBoolean("fromSignUp", false) ?: false

        btnBack.setOnClickListener {
            if (fromSignUp) {
                // 회원가입에서 온 경우: Fragment 제거
                parentFragmentManager.beginTransaction()
                    .remove(this)
                    .commit()
            } else {
                // StartActivity에서 직접 온 경우: 백스택 pop
                parentFragmentManager.popBackStack()
            }
        }

        // 버튼 클릭 이벤트 연결
        loginButton.setOnClickListener {
            val email = emailEdit.text.toString()
            val password = passwordEdit.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.signIn(email, password)
            }
        }
    }
}