buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.agp)
        classpath(libs.kotlin.gradle)
        classpath(libs.compose.gradle)
        classpath(libs.maven.publish)
    }
}

subprojects {
    afterEvaluate {
        extensions.findByType(com.android.build.api.dsl.CommonExtension::class)?.apply {
            buildFeatures {
                buildConfig = false
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }
        }

        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_1_8.toString()
                freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
            }
        }
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
