plugins {
    id("java")
    id("com.gradleup.shadow") version "9.6.1"
}

dependencies {
    implementation(project(":fpirc-common"))

    implementation("io.netty:netty-all:4.2.18.Final")
}