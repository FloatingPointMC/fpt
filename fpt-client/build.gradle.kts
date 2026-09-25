plugins {
    id("java")
}

dependencies {
    api(project(":fpt-common"))
    api(project(":fpt-transport-netty"))

    testImplementation(project(":fpt-common"))
    testImplementation(project(":fpt-transport-netty"))
}