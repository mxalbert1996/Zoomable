plugins {
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.kotlin.plugin.compose) apply false
    alias(libs.plugins.kotlin.binaryCompatibilityValidator) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.maven.publish.base) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.agp)
        classpath(libs.kotlin.gradle)
    }
}

tasks.withType<Delete> {
    delete(rootProject.layout.buildDirectory)
}
