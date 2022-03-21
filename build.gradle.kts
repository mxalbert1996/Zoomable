import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.android.build.api.dsl.CommonExtension as AndroidCommonExtension

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

subprojects {
    afterEvaluate {
        extensions.findByType(AndroidCommonExtension::class)?.apply {
            buildFeatures {
                buildConfig = false
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }
        }

        tasks.withType<KotlinCompile> {
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
