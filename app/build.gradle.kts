// 코틀린 2.0 버전 이상에서는 kotlinOptions 대신 compilerOptions DSL을 사용
//import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Serialization 플러그인 추가 -> // 당신의 Kotlin 버전과 일치시키기
    // Supabase와 같은 백엔드 서비스를 연동할 때 Serialization(직렬화) 필수
    // 객체 (Kotlin): User(name="Gemini", age=25)
    // 직렬화 (JSON): {"name":"Gemini","age":25}
    kotlin("plugin.serialization") version "2.0.21"
}

// 슈퍼베이스 url, key => .local.properties에 추가되어있어야 함
val supabaseUrl = providers.gradleProperty("SUPABASE_URL")
    .orNull ?: (project.findProperty("SUPABASE_URL") as String?)
val supabaseAnonKey = providers.gradleProperty("SUPABASE_ANON_KEY")
    .orNull ?: (project.findProperty("SUPABASE_ANON_KEY") as String?)

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
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.0"))

    // PostgREST (DB까지 쓰면)
    // Auth(이메일/비밀번호 로그인, 이메일 인증 등)
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")

    // Ktor HTTP 엔진(Android)
    // 공식 문서: implementation("io.ktor:ktor-client-[engine]:KTOR_VERSION")
    implementation("io.ktor:ktor-client-okhttp:3.0.0")

    // 실시간 -> 공식 문서 https://supabase.com/docs/reference/kotlin/neq 참고
    implementation("io.github.jan-tennert.supabase:realtime-kt")

    // Desugaring
    // minSdk를 24로 유지할 경우 -> minSDK를 올려서 주석처리함
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}