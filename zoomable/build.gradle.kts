import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.binaryCompatibilityValidator)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish.base)
}

kotlin {
    jvm()

    android {
        publishLibraryVariants("release")
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val commonMain by getting {
            dependencies {
                compileOnly(libs.compose.runtime.jetbrains)
                compileOnly(libs.compose.foundation.jetbrains)
                compileOnly(libs.compose.ui.util.jetbrains)
            }
        }
        val commonTest by getting

        val jvmMain by getting {
            dependencies {
                implementation(libs.compose.runtime.jetbrains)
                implementation(libs.compose.foundation.jetbrains)
                implementation(libs.compose.ui.util.jetbrains)
            }
        }
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(libs.jUnit)
                implementation(libs.truth)
                implementation(libs.coroutines.test)
            }
        }

        val androidMain by getting {
            dependencies {
                // Directly depend on Jetpack Compose for Android
                implementation(libs.compose.runtime.jetpack)
                implementation(libs.compose.foundation.jetpack)
                implementation(libs.compose.ui.util.jetpack)
            }
        }
        val androidInstrumentedTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(libs.truth)
                implementation(libs.compose.ui.test.junit4)
                implementation(libs.compose.ui.test.manifest)
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.compose.runtime.jetbrains)
                implementation(libs.compose.foundation.jetbrains)
                implementation(libs.compose.ui.util.jetbrains)
            }
        }
    }
}

tasks.withType(KotlinCompile::class).configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
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
        unitTests.isReturnDefaultValues = true
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
                "$buildDir/repos/snapshots"
            } else {
                "$buildDir/repos/releases"
            }
        )
    }
}
