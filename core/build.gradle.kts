plugins {
    `java-library`
    `maven-publish`
}

base.archivesName = "felt-core"

tasks.withType<JavaCompile>().configureEach {
    options.release = 8
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.test {
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

publishing {
    publications {
        create<MavenPublication>("library") {
            artifactId = "felt-core"
            from(components["java"])
        }
    }
}
