plugins {
    id("java")
}

dependencies {
    implementation(project(":fpt-common"))
    implementation(project(":fpt-transport-netty"))

    testImplementation(project(":fpt-common"))
    testImplementation(project(":fpt-transport-netty"))
}