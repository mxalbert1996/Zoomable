import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.binaryCompatibilityValidator)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish.base)
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
    namespace = "com.mxalbert.zoomable.zoomable"
    compileSdk = libs.versions.sdk.compile.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    buildFeatures.compose = true

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
        targetSdk = libs.versions.sdk.target.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.jetpack.get()
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
