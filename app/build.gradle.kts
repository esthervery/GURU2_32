// local.properties 읽기 위함
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // Supabase와 같은 백엔드 서비스를 연동할 때 Serialization(직렬화) 필수
    // 객체 (Kotlin): User(name="Gemini", age=25)
    // 직렬화 (JSON): {"name":"Gemini","age":25}
    // 버전을 2.0.21에서 2.1.0으로 변경
    kotlin("plugin.serialization") version "2.1.0"
}

// 1. local.properties 파일을 명시적으로 불러옵니다.
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

// 2. 파일에서 값을 추출합니다. (없을 경우 빈 문자열)
val supabaseUrl = localProperties.getProperty("SUPABASE_URL") ?: ""
val supabaseAnonKey = localProperties.getProperty("SUPABASE_ANON_KEY") ?: ""

android {
    namespace = "com.android.guru2"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        // supabase
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")

        applicationId = "com.android.guru2"
        // supabase 위해서 24 -> 26으로 업
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        // supabase-kt 쪽은 Android 26 이상을 권장, 그대로 24 유지할 경우
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
//    kotlin {
//        compilerOptions {
//            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
//        }
//    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // 테스트용 라이브러리들
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // 슈퍼베이스 implementation("그룹:이름:버전")
    // 3.0.0 이후: auth-kt 모듈 + Ktor 3.x
    // Supabase BOM (버전은 여기서 한 번만 관리)
    // implementation(platform("io.github.jan-tennert.supabase:bom:3.0.0"))
    implementation("io.github.jan-tennert.supabase:supabase-kt:3.2.6")
    implementation("io.github.jan-tennert.supabase:auth-kt:3.2.6")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.2.6")
    implementation("io.github.jan-tennert.supabase:storage-kt:3.2.6")

    // Ktor 3.3.0 (호환 가능)
    implementation("io.ktor:ktor-client-android:3.3.0")

    // Kotlin Serialization JSON: @Serializable 어노테이션을 실제로 동작하게 함
    // Supabase 3.2.6 버전과 호환되는 라이브러리입니다.
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // 실시간 -> 공식 문서 https://supabase.com/docs/reference/kotlin/neq 참고
    // implementation("io.github.jan-tennert.supabase:realtime-kt")

    // Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // 1. Retrofit: 서버 통신을 위한 핵심 라이브러리
    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    // 2. Converter-Gson: 서버 응답(JSON)을 코틀린 객체로 자동 변환
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // 3. Gson: JSON 데이터 처리를 위한 라이브러리
    implementation("com.google.code.gson:gson:2.10.1")

    // 4. OkHttp Logging Interceptor (선택): 서버와 주고받는 데이터를 로그로 확인하고 싶을 때 유용
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Glide: 서버의 캐릭터 URL을 ImageView에 그리기 위해 필수
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // 회원가입 및 로그인 시 사용하는 viewModels()를 위해 추가
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.activity:activity-ktx:1.9.3")
    // 뷰 모델 라이프사이클 관리용
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")
}