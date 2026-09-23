plugins {
    id("java")
}

group = "io.github.floatingpointmc"
version = "0.1.0"

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}