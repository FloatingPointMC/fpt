plugins {
    id("java")
}

dependencies {
    implementation(project(":fpirc-common"))

    implementation("io.netty:netty-all:4.2.18.Final")
}