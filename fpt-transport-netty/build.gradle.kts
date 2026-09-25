plugins {
    id("java")
}

dependencies {
    api(project(":fpt-common"))
    api("io.netty:netty-all:4.2.18.Final")
}