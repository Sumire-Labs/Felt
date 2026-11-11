import org.gradle.api.publish.maven.MavenPublication

plugins {
    base
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion = JavaLanguageVersion.of(17)
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = 17
    }

    tasks.withType<Javadoc>().configureEach {
        (options as StandardJavadocDocletOptions).encoding = "UTF-8"
        (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:none", true)
    }

    tasks.withType<Jar>().configureEach {
        includeEmptyDirs = false
        from(rootProject.layout.projectDirectory.file("LICENSE.md")) {
            into("META-INF")
        }
    }

    dependencies {
        "testImplementation"("junit:junit:4.13.2")
    }

    extensions.configure<PublishingExtension> {
        publications.withType<MavenPublication>().configureEach {
            pom {
                name = "felt"
                description = "License files and source headers for Java projects."
                licenses {
                    license {
                        name = "Blue Oak Model License 1.0.0"
                        url = "https://spdx.org/licenses/BlueOak-1.0.0.html"
                    }
                }
            }
        }
        val repositoryUrl = providers.gradleProperty("maven_url")
        if (repositoryUrl.isPresent) {
            repositories {
                maven {
                    name = "felt"
                    url = uri(repositoryUrl.get())
                    credentials(PasswordCredentials::class)
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(subprojects.map { "${it.path}:check" })
}

tasks.named("assemble") {
    dependsOn(subprojects.map { "${it.path}:assemble" })
}
