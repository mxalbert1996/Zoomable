plugins {
    id("com.android.library")
    kotlin("android")
}

kotlin {
    explicitApi()
}

android {
    compileSdk = libs.versions.sdk.compile.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
        targetSdk = libs.versions.sdk.target.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packagingOptions {
        resources.excludes += "META-INF/*"
    }
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.compose.foundation)

    testImplementation(libs.jUnit)
    testImplementation(libs.coroutines.test)
}
