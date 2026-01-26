package com.android.guru2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.guru2.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private val authViewModel: AuthViewModel by viewModels()
    private var confirmDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailEdit = view.findViewById<EditText>(R.id.editSignupEmail)
        val passwordEdit = view.findViewById<EditText>(R.id.editSignupPassword)
        val signUpButton = view.findViewById<Button>(R.id.btnSignup)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 1. 회원가입 요청 성공 시 안내 팝업 띄우기
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.signUpSuccess.collectLatest { success ->
                if (success == true) {
                    showConfirmEmailDialog()
                } else if (success == false) {
                    Toast.makeText(requireContext(), "회원가입 실패. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 2. 이메일 인증 완료(Authenticated) 감지 시 화면 이동 -> login 화면으로 이동하도록 수정
//        viewLifecycleOwner.lifecycleScope.launch {
//            authViewModel.isAuthenticated.collect { authenticated ->
//                if (authenticated) {
//                    confirmDialog?.dismiss() // 다이얼로그 닫기
//                    val intent = Intent(requireContext(), PetInfoActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    startActivity(intent)
//                }
//            }
//        }

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

    private fun showConfirmEmailDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_email, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)

        confirmDialog = builder.create()
        confirmDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btn_dialog_close).setOnClickListener {
            confirmDialog?.dismiss()
            // 먼저 백스택에서 SignUpFragment의 기록을 지움
            parentFragmentManager.popBackStack()

            // LoginFragment 생성 시 "회원가입에서 왔다" 표시
            val loginFragment = LoginFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("fromSignUp", true)
                }
            }
            (activity as? StartActivity)?.replaceFragment(loginFragment)
        }

        confirmDialog?.show()
    }
}