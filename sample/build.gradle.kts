plugins {
    kotlin("android")
    id("com.android.application")
}

android {
    namespace = "com.mxalbert.zoomable.sample"
    compileSdk = libs.versions.sdk.compile.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    buildFeatures.compose = true

    defaultConfig {
        applicationId = "com.mxalbert.zoomable.sample"
        minSdk = libs.versions.sdk.min.get().toInt()
        targetSdk = libs.versions.sdk.target.get().toInt()
        versionName = property("VERSION_NAME") as String
        versionCode = 1
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.jetpack.get()
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(project(":zoomable"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.material)
    implementation(libs.bundles.accompanist)
    implementation(libs.coil.compose)
}
