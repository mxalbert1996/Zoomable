import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    jvm("desktop")

    android()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = property("VERSION_NAME") as String
        summary = property("POM_DESCRIPTION") as String
        homepage = property("POM_URL") as String
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
        }
    }

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":zoomable"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
            }
        }

        val nonAndroidMain by creating {
            dependsOn(commonMain)
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

        val androidMain by getting {
            dependencies {
                implementation(libs.coil.compose)
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            dependsOn(nonAndroidMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.ktor.client.darwin)
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
