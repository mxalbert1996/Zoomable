import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.plugin.compose)
}

kotlin {
    jvm("desktop")

    androidTarget()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.library()
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":zoomable"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
        }

        val nonAndroidMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.ktor.client.core)
            }
        }

        val desktopMain by getting {
            dependsOn(nonAndroidMain)
            dependencies {
                implementation(libs.ktor.client.java)
            }
        }

        androidMain.dependencies {
            implementation(libs.coil.compose)
        }

        iosMain {
            dependsOn(nonAndroidMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        val wasmJsMain by getting {
            dependsOn(nonAndroidMain)
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
    }
}

tasks.withType(KotlinCompile::class).configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

android {
    namespace = "com.mxalbert.zoomable.sample.shared"
    compileSdk = libs.versions.sdk.compile.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    buildFeatures.buildConfig = false

    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packagingOptions.resources.pickFirsts.add("META-INF/*")
}
