import java.net.HttpURLConnection
import java.net.URI
import java.util.Base64

import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension

plugins {
    id("java")
}

subprojects {
    apply {
        plugin("java-library")
        plugin("maven-publish")
        plugin("signing")
    }

    group = "io.github.floatingpointmc"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }

        withSourcesJar()
        withJavadocJar()
    }

    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])

                pom {
                    name.set(
                        when (project.name) {
                            "fpt-common" -> "FloatingPointTransport-Common"
                            "fpt-client" -> "FloatingPointTransport-Client"
                            "fpt-server" -> "FloatingPointTransport-Server"
                            "fpt-transport-netty" -> "FloatingPointTransport-Netty"
                            else -> "FloatingPointTransport-${project.name}"
                        }
                    )
                    description.set(
                        "An embeddable and extensible Java network communication and protocol framework."
                    )

                    url.set("https://github.com/floatingpointmc/fpt")
                    inceptionYear.set("2026")

                    licenses {
                        license {
                            name.set("LGPL-3.0")
                            url.set("https://opensource.org/license/lgpl-3.0")
                        }
                    }

                    developers {
                        developer {
                            id.set("floatingpointmc")
                            name.set("FloatingPoint-MC")
                            email.set("vlouyearlinjinhua@outlook.com")
                        }
                    }

                    scm {
                        url.set("https://github.com/floatingpointmc/fpt")
                        connection.set(
                            "scm:git:git://github.com/floatingpointmc/fpt.git"
                        )
                        developerConnection.set(
                            "scm:git:ssh://github.com/floatingpointmc/fpt.git"
                        )
                    }
                }
            }
        }

        repositories {
            maven {
                name = "central"

                url = uri(
                    "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/"
                )

                credentials {
                    username = findProperty("ossrhUsername") as? String
                    password = findProperty("ossrhPassword") as? String
                }
            }
        }
    }

    extensions.configure<SigningExtension> {
        useGpgCmd()

        sign(
            extensions
                .getByType<PublishingExtension>()
                .publications["mavenJava"]
        )
    }

    val publishToCentralPortal = tasks.register("publishToCentralPortal") {
        group = "publishing"
        description =
            "Transfers the OSSRH staging repository to the Central Publisher Portal."

        doLast {
            val username = findProperty("ossrhUsername") as? String
                ?: throw GradleException(
                    "Missing ossrhUsername in gradle.properties"
                )

            val password = findProperty("ossrhPassword") as? String
                ?: throw GradleException(
                    "Missing ossrhPassword in gradle.properties"
                )

            val namespace = project.group.toString()

            val token = Base64.getEncoder().encodeToString(
                "$username:$password".toByteArray(Charsets.UTF_8)
            )

            val url = URI(
                "https://ossrh-staging-api.central.sonatype.com" +
                        "/manual/upload/defaultRepository/" +
                        "$namespace?publishing_type=automatic"
            ).toURL()

            println()
            println("Submitting deployment to Maven Central...")
            println("Namespace: $namespace")

            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "POST"

                connection.setRequestProperty(
                    "Authorization",
                    "Bearer $token"
                )

                connection.setRequestProperty(
                    "Accept",
                    "application/json"
                )

                connection.doOutput = true

                connection.outputStream.use { }

                val responseCode = connection.responseCode

                val response = try {
                    connection.inputStream
                        .bufferedReader()
                        .use { it.readText() }
                } catch (_: Exception) {
                    connection.errorStream
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        ?: ""
                }

                if (responseCode !in 200..299) {
                    throw GradleException(
                        """
                        Failed to submit deployment to Central.

                        HTTP $responseCode
                        $response
                        """.trimIndent()
                    )
                }

                println("Central deployment submitted successfully.")

                if (response.isNotBlank()) {
                    println(response)
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    tasks.named("publish") {
        finalizedBy(publishToCentralPortal)
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")

        compileOnly("org.projectlombok:lombok:1.18.36")
        annotationProcessor("org.projectlombok:lombok:1.18.36")

        compileOnly("org.jetbrains:annotations:26.1.0")
        annotationProcessor("org.jetbrains:annotations:26.1.0")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}