/*
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: BlueOak-1.0.0
 */

package com.sumirelabs.felt.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.tasks.GenerateMavenPom;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

public final class FeltPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        FeltExtension extension = project.getExtensions().create("felt", FeltExtension.class);
        extension.getLicense().convention(project.getProviders().gradleProperty("license"));
        extension.getAuthor().convention(project.getProviders().gradleProperty("license_author"));
        extension.getYear().convention(project.getProviders().gradleProperty("license_year"));
        extension.getLicenseFile().convention(project.getLayout().getProjectDirectory().file("LICENSE.md"));
        extension.getHeaderFile().convention(project.getLayout().getProjectDirectory().file("HEADER"));

        TaskProvider<GenerateLicenseHeader> generateHeader = project.getTasks().register(
                "generateLicenseHeader", GenerateLicenseHeader.class, task -> {
                    configure(task, extension, "Generate HEADER without replacing an existing header.");
                    task.getHeaderFile().set(extension.getHeaderFile());
                });

        TaskProvider<GenerateLicense> generate = project.getTasks().register("generateLicense", GenerateLicense.class, task -> {
            configure(task, extension, "Generate LICENSE.md and HEADER without replacing existing notices.");
            task.getLicenseFile().set(extension.getLicenseFile());
            task.dependsOn(generateHeader);
        });
        TaskProvider<ApplyLicenseHeader> apply = project.getTasks().register("applyLicenseHeader", ApplyLicenseHeader.class, task -> {
            configure(task, extension, "Add license headers to Java sources.");
            task.getSources().from(extension.getSources());
            task.getHeaderFile().set(extension.getHeaderFile());
            task.dependsOn(generateHeader);
        });
        TaskProvider<CheckLicense> check = project.getTasks().register("checkLicense", CheckLicense.class, task -> {
            configure(task, extension, "Check LICENSE.md and Java source headers.");
            task.getLicenseFile().set(extension.getLicenseFile());
            task.getHeaderFile().set(extension.getHeaderFile());
            task.getSources().from(extension.getSources());
            task.mustRunAfter(generate, generateHeader, apply);
        });

        project.getTasks().withType(JavaCompile.class).configureEach(task -> task.mustRunAfter(apply));

        project.getPluginManager().withPlugin("java", ignored -> {
            JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);
            java.getSourceSets().all(sourceSet -> extension.getSources().from(
                    sourceSet.getAllJava().getSourceDirectories().getAsFileTree().matching(pattern -> {
                        pattern.include("**/*.java");
                    })));
        });
        project.getPluginManager().withPlugin("base", ignored ->
                project.getTasks().named("check").configure(task -> task.dependsOn(check)));

        project.getPluginManager().withPlugin("maven-publish", ignored -> {
            PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
            publishing.getPublications().withType(MavenPublication.class).configureEach(publication ->
                    publication.getPom().withXml(new PomLicenseAction(extension.getLicense())));
            project.getTasks().withType(GenerateMavenPom.class).configureEach(task ->
                    task.getInputs().property("felt.license", extension.getLicense()));
        });
    }

    private static void configure(LicenseTask task, FeltExtension extension, String description) {
        task.setGroup("license");
        task.setDescription(description);
        task.getLicense().set(extension.getLicense());
        task.getAuthor().set(extension.getAuthor());
        task.getYear().set(extension.getYear());
    }
}
