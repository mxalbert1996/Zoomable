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

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
