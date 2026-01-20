// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // libs.versions.toml에 정의된 kotlin-android 플러그인(및 버전)을 가져다 쓰겠다
    alias(libs.plugins.kotlin.android) apply false
}