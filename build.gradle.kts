plugins {
    kotlin("multiplatform") version "1.9.0-Beta"
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        withJava()
    }
    val osName = System.getProperty("os.name")
    when {
        osName.contains("Windows") -> mingwX64("native")
        osName.contains("MacOS") -> macosX64("native")
        else -> linuxX64("native")
    }
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("common/src")
        }
        val jvmMain by getting {
            kotlin.srcDir("jvm/src")
        }
        val nativeMain by getting {
            kotlin.srcDir("native/src")
        }
        val commonTest by getting {
            kotlin.srcDir("common/test")
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmTest by getting {
            kotlin.srcDir("jvm/test")
        }
        val nativeTest by getting {
            kotlin.srcDir("native/test")
        }
    }
    jvmToolchain(17)
}

