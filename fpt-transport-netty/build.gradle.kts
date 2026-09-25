plugins {
    id("java")
}

dependencies {
    api(project(":fpt-common"))

    implementation("io.netty:netty-all:4.2.18.Final")
}