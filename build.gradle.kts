plugins {
    id("java")
}

group = "io.github.floatingpointmc"
version = "0.1.0"

subprojects {
    apply { plugin("java-library") }

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.36")
        annotationProcessor("org.projectlombok:lombok:1.18.36")
        compileOnly("org.jetbrains:annotations:26.1.0")
        annotationProcessor("org.jetbrains:annotations:26.1.0")

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}