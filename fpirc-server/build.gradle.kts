plugins {
    id("java")
    id("application")
}

application {
    mainClass.set("io.github.floatingpointmc.fpirc.demo.IntegrationDemo")
}

dependencies {
    implementation(project(":fpirc-common"))
    implementation(project(":fpirc-client"))

    implementation("io.netty:netty-all:4.1.138.Final")
}