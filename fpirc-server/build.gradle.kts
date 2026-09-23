plugins {
    id("java")
    id("application")
    id("com.gradleup.shadow") version "9.6.1"
}

application {
    mainClass.set("io.github.floatingpointmc.fpirc.demo.IntegrationDemo")
}

dependencies {
    implementation(project(":fpirc-common"))

    implementation("io.netty:netty-all:4.2.18.Final")
}