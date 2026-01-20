package com.android.guru2

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.android.guru2.data.SupabaseClientProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

class PetInfoActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private var selectedGender: Int? = null // 0: 여, 1: 남

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_info)

        val ivProfile = findViewById<ImageView>(R.id.iv_pet_profile)
        val btnAddPhoto = findViewById<FloatingActionButton>(R.id.btn_add_photo)
        val etName = findViewById<TextInputEditText>(R.id.et_pet_name)
        val etAge = findViewById<TextInputEditText>(R.id.et_pet_age)
        val ivFemale = findViewById<ImageView>(R.id.iv_female)
        val ivMale = findViewById<ImageView>(R.id.iv_male)
        val btnSubmit = findViewById<Button>(R.id.btn_submit)

        // 1. 갤러리 연동 (카메라 기능 제외)
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedImageUri = it
                ivProfile.setImageURI(it)
            }
        }
        btnAddPhoto.setOnClickListener { pickImage.launch("image/*") }

        // 2. 성별 선택 토글 로직
        ivFemale.setOnClickListener {
            selectedGender = 0
            ivFemale.alpha = 1.0f // 선택 시 불투명
            ivMale.alpha = 0.5f   // 미선택 시 반투명
        }
        ivMale.setOnClickListener {
            selectedGender = 1
            ivMale.alpha = 1.0f
            ivFemale.alpha = 0.5f
        }

        // 3. 시작하기 버튼 클릭 (DB 전송)
        btnSubmit.setOnClickListener {
            val name = etName.text.toString()
            val ageStr = etAge.text.toString()

            // [검증] 하나라도 빠지면 넘어가지 않음
            if (selectedImageUri == null || name.isEmpty() || ageStr.isEmpty() || selectedGender == null) {
                Toast.makeText(this, "모든 정보를 입력해 주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadAndSave(name, ageStr.toInt(), selectedGender!!)
        }
    }

    private fun uploadAndSave(name: String, age: Int, gender: Int) {
        lifecycleScope.launch {
            try {
                // 1. 현재 로그인한 사용자의 ID(UUID) 가져오기
                val currentUser = SupabaseClientProvider.client.auth.currentUserOrNull()
                val userId = currentUser?.id ?: throw Exception("로그인 정보가 없습니다.")

                // 2. 이미지 업로드 (기존 로직 동일)
                val fileName = "$userId/${System.currentTimeMillis()}.jpg" // 사용자별 폴더 관리 권장
                val bytes = contentResolver.openInputStream(selectedImageUri!!)?.readBytes() ?: return@launch
                val bucket = SupabaseClientProvider.client.storage.from("pet_images")

                bucket.upload(fileName, bytes)
                val publicUrl = bucket.publicUrl(fileName)

                // 3. DB 저장 (가져온 userId를 id로 사용)
                val petData = PetInfo(
                    id = userId, // auth.users.id와 매칭
                    pet_name = name,
                    pet_age = age,
                    is_male = gender,
                    pet_image = publicUrl
                )

                SupabaseClientProvider.client.postgrest["petInfo"].insert(petData)

                Toast.makeText(this@PetInfoActivity, "반려동물 등록 성공!", Toast.LENGTH_SHORT).show()
                // 메인 화면으로 이동
            } catch (e: Exception) {
                Toast.makeText(this@PetInfoActivity, "오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}