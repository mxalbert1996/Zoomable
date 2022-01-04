import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.library")
    kotlin("android")
    alias(libs.plugins.kotlin.binaryCompatibilityValidator)
    id("com.vanniktech.maven.publish")
}

kotlin {
    explicitApi()
    sourceSets {
        test {
            kotlin.srcDir("src/sharedTest/java")
        }
        androidTest {
            kotlin.srcDir("src/sharedTest/java")
        }
    }
}

android {
    compileSdk = libs.versions.sdk.compile.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
        targetSdk = libs.versions.sdk.target.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    packagingOptions.resources.pickFirsts.add("META-INF/*")
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui.util)

    testImplementation(libs.jUnit)
    testImplementation(libs.truth)
    testImplementation(libs.coroutines.test)

    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.compose.ui.test.manifest)
}

mavenPublish {
    sonatypeHost = SonatypeHost.S01
}

publishing {
    repositories {
        val version = property("VERSION_NAME") as String
        maven(
            url = if (version.endsWith("SNAPSHOT")) {
                "$buildDir/repos/snapshots"
            } else {
                "$buildDir/repos/releases"
            }
        )
    }
}
