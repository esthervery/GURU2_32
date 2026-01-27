package com.android.guru2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.android.guru2.data.SupabaseClientProvider
import com.android.guru2.network.RetrofitClient
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PetInfoActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null

    // 성별 정보 저장 (0: 여자아이, 1: 남자아이)
    private var selectedGender: Int? = null

    // 서버 공개 URL 저장 (반려동물 이미지)
    private var existingImageUrl: String? = null

    // 이미지 선택 (+ 버튼 클릭 시)
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            findViewById<ImageView>(R.id.iv_pet_profile).setImageURI(it)
        }
    }

    // 권한 요청
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 권한 허용됨 -> 이미지 선택
            pickImage.launch("image/*")
        } else {
            // 권한 거부됨 -> 안내 메세지 출력
            Toast.makeText(this, "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

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

        // DB에 저장된 기존 정보가 있다면 불러오기
        loadExistingPetInfo(etName, etAge, ivFemale, ivMale, ivProfile)

        // 사진 추가 버튼 클릭
        btnAddPhoto.setOnClickListener {
            checkAndRequestPermission()
        }

        // 성별 선택
        ivFemale.setOnClickListener {
            selectedGender = 0
            // 선택 시 불투명
            ivFemale.alpha = 1.0f
            // 미선택 시 반투명
            ivMale.alpha = 0.5f
        }
        ivMale.setOnClickListener {
            selectedGender = 1
            ivMale.alpha = 1.0f
            ivFemale.alpha = 0.5f
        }

        // 시작하기 버튼 클릭 (DB 전송)
        btnSubmit.setOnClickListener {
            val name = etName.text.toString()
            val ageStr = etAge.text.toString()

            // 정보가 하나라도 빠지면 넘어가지 않도록 설정
            if ((selectedImageUri == null && existingImageUrl == null) ||
                name.isEmpty() || ageStr.isEmpty() || selectedGender == null) {
                Toast.makeText(this, "모든 정보를 입력해 주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 전체 프로세스 시작
            startPetRegistration(name, ageStr.toInt(), selectedGender!!)
        }
    }

    // DB에서 기존 반려동물 정보 조회하여 UI에 세팅하는 함수
    private fun loadExistingPetInfo(
        etName: TextInputEditText,
        etAge: TextInputEditText,
        ivFemale: ImageView,
        ivMale: ImageView,
        ivProfile: ImageView
    ) {
        lifecycleScope.launch {
            try {
                // 현재 사용자 ID 획득
                val currentUser = SupabaseClientProvider.client.auth.currentUserOrNull()
                val userId = currentUser?.id ?: return@launch

                // petInfo 테이블에서 해당 유저의 row 조회
                val petData = SupabaseClientProvider.client.postgrest["petInfo"]
                    .select {
                        filter { eq("id", userId) }
                    }.decodeSingleOrNull<PetInfo>()

                // 데이터가 존재하면 UI에 반영
                petData?.let { data ->
                    etName.setText(data.pet_name)
                    etAge.setText(data.pet_age.toString())

                    // 성별 세팅 (0: 여자, 1: 남자)
                    selectedGender = data.is_male
                    if (data.is_male == 0) {
                        ivFemale.alpha = 1.0f
                        ivMale.alpha = 0.5f
                    } else {
                        ivMale.alpha = 1.0f
                        ivFemale.alpha = 0.5f
                    }

                    // 이미지 로드
                    existingImageUrl = data.pet_image
                    Glide.with(this@PetInfoActivity)
                        .load(data.pet_image)
                        .circleCrop()
                        .into(ivProfile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 데이터가 없는 경우 -> 별도 에러 처리는 생략
            }
        }
    }


    private fun checkAndRequestPermission() {
        when {
            // Android 13 (API 33) 이상
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // 권한 O: 이미지 선택
                        pickImage.launch("image/*")
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES) -> {
                        // 이전에 권한 거부한 경우 -> 다시 요청 가능하도록
                        Toast.makeText(this, "사진을 선택하려면 갤러리 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show()
                        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                    else -> {
                        // 권한 처음 요청
                        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                }
            }
            // Android 10-12 (API 29-32)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Android 10+ 는 Scoped Storage 사용 - 권한 불필요
                pickImage.launch("image/*")
            }
            // Android 9 이하 (API 28 이하)
            else -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        pickImage.launch("image/*")
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            }
        }
    }

    private fun startPetRegistration(name: String, age: Int, gender: Int) {
        lifecycleScope.launch {
            try {
                // 로딩 화면 표시
                showLoadingOverlay()

                // DB 업데이트 분기 처리
                val finalImageUrl = if (selectedImageUri != null) {
                    // 새 이미지를 선택했다면 -> 업로드 후 새 URL 획득
                    uploadImageAndSaveToDB(name, age, gender)
                } else {
                    // 이미지를 바꾸지 않았다면 -> 기존 URL 사용 및 DB 정보만 업데이트
                    updateOnlyPetInfo(name, age, gender, existingImageUrl!!)
                    existingImageUrl!!
                }

                // 캐릭터 생성 서버 호출
                requestCharacterGeneration(finalImageUrl)

            } catch (e: Exception) {
                hideLoadingOverlay()
                Toast.makeText(
                    this@PetInfoActivity,
                    "오류 발생: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }
    }
    private suspend fun updateOnlyPetInfo(name: String, age: Int, gender: Int, imageUrl: String) {
        val currentUser = SupabaseClientProvider.client.auth.currentUserOrNull() ?: return
        val petData = PetInfo(currentUser.id, name, age, gender, imageUrl)

        // upsert를 통해 기존 정보를 업데이트
        SupabaseClientProvider.client.postgrest["petInfo"].upsert(petData)
    }

    private suspend fun uploadImageAndSaveToDB(name: String, age: Int, gender: Int): String {
        try {
            // 현재 사용자 확인
            val currentUser = SupabaseClientProvider.client.auth.currentUserOrNull()
                ?: throw Exception("로그인 정보가 없습니다.")
            val userId = currentUser.id

            // 이미지를 ByteArray로 읽기
            val imageBytes = contentResolver.openInputStream(selectedImageUri!!)?.use { inputStream ->
                inputStream.readBytes()
            } ?: throw Exception("이미지를 읽을 수 없습니다.")

            // Storage에 업로드
            val fileName = "$userId/${System.currentTimeMillis()}.jpg"
            val bucket = SupabaseClientProvider.client.storage.from("pet_images")

            bucket.upload(fileName, imageBytes)
            val publicUrl = bucket.publicUrl(fileName)

            // DB에 펫 정보 저장 (덮어쓰기 가능하도록 upsert 사용)
            val petData = PetInfo(userId, name, age, gender, publicUrl)
            SupabaseClientProvider.client.postgrest["petInfo"].upsert(petData)

            Toast.makeText(this, "반려동물 정보가 저장되었습니다.", Toast.LENGTH_SHORT).show()

            return publicUrl

        } catch (e: Exception) {
            throw Exception("이미지 업로드 실패: ${e.localizedMessage}", e)
        }
    }

    private suspend fun requestCharacterGeneration(imageUrl: String) {
        try {
            val currentUser = SupabaseClientProvider.client.auth.currentUserOrNull()
                ?: throw Exception("로그인 정보가 없습니다.")
            val userId = currentUser.id

            val service = RetrofitClient.instance.create(CharacterApiService::class.java)

            // 서버 호출
            val response = service.requestCharacterGeneration(userId, imageUrl)

            if (response.isSuccessful) {
                // 서버 호출 및 이미지 반환 성공: MainActivity로 이동
                hideLoadingOverlay()
                Toast.makeText(this, "캐릭터 생성 요청이 완료되었습니다!", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            } else {
                // 서버 응답 오류 (호출은 O)
                throw Exception("서버 응답 오류: ${response.code()} ${response.message()}")
            }

        } catch (e: Exception) {
            // 서버 호출 실패
            hideLoadingOverlay()
            Toast.makeText(
                this,
                "캐릭터 생성 요청 실패: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }

    // 메인 액티비티로 이동
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // 로딩 프래그먼트 표시
    private fun showLoadingOverlay() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.loading_container, LoadingFragment())
            .commit()
    }

    // 서버 호출 실패 시 로딩 프래그먼트 제거
    private fun hideLoadingOverlay() {
        val fragment = supportFragmentManager.findFragmentById(R.id.loading_container)
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }
}