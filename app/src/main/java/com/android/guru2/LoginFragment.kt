package com.android.guru2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.guru2.AuthViewModel
import kotlinx.coroutines.launch

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

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.loginEvent.collect { event ->
                when (event) {
                    is LoginNavEvent.ToMain -> {
                        // 정보가 있으면 메인으로
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    is LoginNavEvent.ToPetInfo -> {
                        // 정보가 없으면 등록 화면으로
                        val intent = Intent(requireContext(), PetInfoActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    is LoginNavEvent.Error -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                    }
                }
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