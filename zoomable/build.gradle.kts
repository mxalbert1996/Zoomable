import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.kotlin.binaryCompatibilityValidator)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish.base)
}

kotlin {
    jvm()

    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            compileOnly(libs.compose.runtime.jetbrains)
            compileOnly(libs.compose.foundation.jetbrains)
            compileOnly(libs.compose.ui.util.jetbrains)
        }

        jvmMain.dependencies {
            implementation(libs.compose.runtime.jetbrains)
            implementation(libs.compose.foundation.jetbrains)
            implementation(libs.compose.ui.util.jetbrains)
        }

        androidMain.dependencies {
            // Directly depend on Jetpack Compose for Android
            implementation(libs.compose.runtime.jetpack)
            implementation(libs.compose.foundation.jetpack)
            implementation(libs.compose.ui.util.jetpack)
        }
        androidUnitTest.dependencies {
            implementation(libs.jUnit)
            implementation(libs.truth)
            implementation(libs.robolectric)
            implementation(libs.test.ext.junit)
            implementation(libs.compose.ui.test.junit4)
            implementation(libs.compose.ui.test.manifest)
        }

        iosMain.dependencies {
            implementation(libs.compose.runtime.jetbrains)
            implementation(libs.compose.foundation.jetbrains)
            implementation(libs.compose.ui.util.jetbrains)
        }
    }
}

android {
    namespace = "com.mxalbert.zoomable.zoomable"
    compileSdk = libs.versions.sdk.compile.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    buildFeatures.buildConfig = false

    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    packagingOptions.resources.pickFirsts.add("META-INF/*")
}

mavenPublishing {
    group = project.property("GROUP") ?: group
    version = project.property("VERSION_NAME") ?: version
    publishToMavenCentral(SonatypeHost.S01)
    signAllPublications()
    pomFromGradleProperties()
    configure(KotlinMultiplatform(JavadocJar.Dokka("dokkaHtml")))
}

publishing {
    repositories {
        val version = property("VERSION_NAME") as String
        maven(
            url = if (version.endsWith("SNAPSHOT")) {
                layout.buildDirectory.dir("repos/snapshots")
            } else {
                layout.buildDirectory.dir("repos/releases")
            }
        )
    }
}
