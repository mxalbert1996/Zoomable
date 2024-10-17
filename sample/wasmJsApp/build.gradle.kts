import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.plugin.compose)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            val rootDirPath = project.rootDir.path
            commonWebpackConfig {
                outputFileName = "zoomable.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    port = 8080
                    static = (static ?: mutableListOf()).apply {
                        add(rootDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }
    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":sample:shared"))
            implementation(libs.compose.foundation.jetbrains)
        }
    }
}
