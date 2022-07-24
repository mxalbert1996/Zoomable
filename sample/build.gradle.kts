import java.io.ByteArrayOutputStream

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
        val libVersion = property("VERSION_NAME") as String
        val isSnapshot = libVersion.endsWith("SNAPSHOT")
        val appVersion = if (isSnapshot) {
            ByteArrayOutputStream().use { os ->
                exec {
                    setCommandLine("git", "describe", "--tags")
                    standardOutput = os
                }
                os.toString()
            }
        } else libVersion
        val (version, commitCount) = if (isSnapshot) {
            val description = appVersion.split('-')
            description[0] to description[1].toInt()
        } else {
            libVersion to 0
        }
        versionName = appVersion
        versionCode = commitCount + version.split('.')
            .fold(0) { result, number -> (result + number.toInt()) * 100 }
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
