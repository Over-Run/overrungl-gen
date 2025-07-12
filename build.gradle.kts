plugins {
    kotlin("multiplatform") version "2.2.0"
}

dependencies {
    commonMainImplementation(kotlin("stdlib-js"))
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-html-js:0.12.0")
}

kotlin {
    js {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        binaries.executable()
    }
}
