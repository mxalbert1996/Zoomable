import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    alias(libs.plugins.jetbrains.compose)
}

tasks.withType(KotlinCompile::class).configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

compose.desktop {
    application {
        mainClass = "com.mxalbert.zoomable.sample.MainKt"
    }
}

dependencies {
    implementation(project(":sample:shared"))
    implementation(compose.desktop.currentOs)
}
