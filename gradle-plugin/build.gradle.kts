plugins {
    `java-gradle-plugin`
    `maven-publish`
}

base.archivesName = "felt-gradle-plugin"

dependencies {
    implementation(project(":core"))
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("felt") {
            id = "com.sumirelabs.felt"
            implementationClass = "com.sumirelabs.felt.gradle.FeltPlugin"
            displayName = "felt"
            description = "Generate and check license files and Java source headers."
        }
    }
}

publishing.publications.withType<MavenPublication>().configureEach {
    if (name == "pluginMaven") {
        artifactId = "felt-gradle-plugin"
    }
}
