plugins {
    kotlin("jvm")
    id("me.champeau.jmh") version "0.7.1"
}

repositories {
    mavenCentral()
}

dependencies {
    "jmhImplementation"(project(":"))
}

sourceSets.jmh {
    java.srcDirs("src")
}