plugins {
    kotlin("android")
    id("com.android.application")
    alias(libs.plugins.kotlin.plugin.compose)
}

android {
    namespace = "com.mxalbert.zoomable.sample"
    compileSdk = libs.versions.sdk.compile.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    buildFeatures {
        buildConfig = false
        compose = true
    }

    defaultConfig {
        applicationId = "com.mxalbert.zoomable.sample"
        minSdk = libs.versions.sdk.min.get().toInt()
        targetSdk = libs.versions.sdk.target.get().toInt()
        androidResources.localeFilters += "en"
        val libVersion = property("VERSION_NAME") as String
        val appVersion = libVersion.substringBefore('-')
        versionName = appVersion
        versionCode = appVersion.split('.')
            .fold(0) { result, number -> (result + number.toInt()) * 100 }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
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

    packaging {
        resources.excludes += listOf(
            "META-INF/*",
            "**/*.kotlin_builtins",
            "DebugProbesKt.bin"
        )
    }
}

dependencies {
    implementation(project(":sample:shared"))
    implementation(libs.androidx.activity.compose)
}
