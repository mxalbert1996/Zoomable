import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    alias(libs.plugins.kotlin.binaryCompatibilityValidator)
    id("com.vanniktech.maven.publish")
}

kotlin {
    android {
        publishLibraryVariants("release")
    }
    jvm()
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
                compileOnly(libs.compose.runtime.jetbrains)
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
                compileOnly(libs.compose.runtime.jetpack)
                implementation(libs.compose.foundation.jetpack)
                implementation(libs.compose.ui.util.jetpack)
            }
        }
        val androidAndroidTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(libs.truth)
                implementation(libs.compose.ui.test.junit4)
                implementation(libs.compose.ui.test.manifest)
            }
        }
        removeAll { sourceSet ->
            sourceSet.name in setOf(
                "androidAndroidTestRelease",
                "androidTestFixtures",
                "androidTestFixturesDebug",
                "androidTestFixturesRelease",
            )
        }
    }
}

android {
    compileSdk = libs.versions.sdk.compile.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

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
