import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
}

// local.properties 파일 불러오기
val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(FileInputStream(f))
}
// 파일에서 값을 추출 (없을 경우 빈 문자열)
val supabaseUrl = localProperties.getProperty("SUPABASE_URL") ?: ""
val supabaseAnonKey = localProperties.getProperty("SUPABASE_ANON_KEY") ?: ""

android {
    namespace = "com.android.guru2"
    compileSdk = 36
    
    // 서연님 코드 반영 (buildFeatures, packaging)
    buildFeatures {
        compose = true
        buildConfig = true // BuildConfig 생성 켜기
    }

    packaging {
        resources { 
          excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    defaultConfig {
        applicationId = "com.android.guru2"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // BuildConfig에 키 주입
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
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
    
    // java 11 -> 21 변경
    compileOptions {
        // supabase-kt 쪽은 Android 26 이상을 권장, 그대로 24 유지할 경우
        isCoreLibraryDesugaringEnabled = true
        
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    
    // Compose 컴파일러 옵션 (선택사항, 안정성 향상)
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8" // Kotlin 2.1.0과 호환
    }
}

dependencies {
    // ===== Core Android =====
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ===== Test Libraries (김에스더) ===== 
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // ===== Compose Test Libraries (서연님) =====
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.01.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    
    // ===== Compose UI (서연님) =====
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    implementation("androidx.compose.material3:material3")

    // ===== Supabase (김에스더 사용 버전으로 통일) ===== 
    // 3.0.0 이후: auth-kt 모듈 + Ktor 3.x
    // implementation(platform("io.github.jan-tennert.supabase:bom:3.0.0"))
    implementation("io.github.jan-tennert.supabase:supabase-kt:3.2.6")
    implementation("io.github.jan-tennert.supabase:auth-kt:3.2.6")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.2.6")
    implementation("io.github.jan-tennert.supabase:storage-kt:3.2.6")

    // ===== Ktor (Supabase 통신용) ===== 3.3.0 (호환 가능 버전)
    implementation("io.ktor:ktor-client-android:3.3.0")

    // ===== Kotlin Serialization =====
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // 실시간 -> 공식 문서 https://supabase.com/docs/reference/kotlin/neq 참고
    // implementation("io.github.jan-tennert.supabase:realtime-kt")


    // ===== Java 8+ API Desugaring =====
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")


    // ===== Retrofit (서버 통신) =====
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")


    // ===== Glide (이미지 로딩) ===== 서버의 캐릭터 URL을 ImageView에 그리기 위해 필수
    implementation("com.github.bumptech.glide:glide:4.16.0")

    
    // ===== Fragment & Activity =====
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.activity:activity-compose:1.9.3") // Compose용 (버전 통일)


    // ===== Lifecycle & ViewModel (XML용) =====
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0") // Compose용 (버전 통일)
    
    
    // ===== Material Design =====
    implementation("com.google.android.material:material:1.11.0")
    
    
    // ===== Navigation =====
    implementation("androidx.navigation:navigation-compose:2.7.6") // Compose용 (서연님)


    // ===== Splash Screen =====
    implementation("androidx.core:core-splashscreen:1.1.0-rc01")
    
    
    // ===== Date/Time =====
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")


    // ===== Coroutines =====
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
