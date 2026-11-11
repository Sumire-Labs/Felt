/*
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: BlueOak-1.0.0
 */

package com.sumirelabs.felt.gradle;

import com.sumirelabs.felt.License;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class FeltPluginTest {

    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void appliesHeadersChecksAndReusesConfigurationCache() throws IOException {
        Path root = fixture();
        Path source = root.resolve("src/main/java/example/Example.java");
        write(source, "package example;\nclass Example {}\n");
        write(root.resolve("build/ignored/Generated.java"), "class Generated {}\n");
        run(root, "generateLicense", "applyLicenseHeader", "check", "--configuration-cache").build();
        String licensed = read(source);
        assertTrue(licensed.contains("SPDX-License-Identifier: MIT"));
        assertEquals("class Generated {}\n", read(root.resolve("build/ignored/Generated.java")));
        run(root, "check", "--configuration-cache").build();
        BuildResult reused = run(root, "check", "--configuration-cache").build();
        assertTrue(reused.getOutput().contains("Reusing configuration cache."));
        run(root, "applyLicenseHeader").build();
        assertEquals(licensed, read(source));
    }

    @Test
    public void checkDoesNotFixMissingHeaders() throws IOException {
        Path root = fixture();
        Path source = root.resolve("src/test/java/example/ExampleTest.java");
        String original = "package example;\nclass ExampleTest {}\n";
        write(source, original);
        run(root, "generateLicense").build();
        BuildResult failed = run(root, "checkLicense").buildAndFail();
        assertTrue(failed.getOutput().contains("Missing or different license header"));
        assertEquals(original, read(source));
    }

    @Test
    public void refusesToReplaceExistingLicenseFile() throws IOException {
        Path root = fixture();
        write(root.resolve("LICENSE.md"), "Existing copyright and license\n");
        BuildResult failed = run(root, "generateLicense").buildAndFail();
        assertTrue(failed.getOutput().contains("Felt will not overwrite it"));
        assertEquals("Existing copyright and license\n", read(root.resolve("LICENSE.md")));
    }

    @Test
    public void supportsAllBuiltInLicensesFromGradleProperties() throws IOException {
        for (String identifier : License.supportedIdentifiers()) {
            if (identifier.equals("MIT")) {
                continue;
            }
            Path root = fixture();
            write(root.resolve("gradle.properties"),
                    "license=" + identifier + "\nlicense_author=Example Authors\nlicense_year=2026\n");
            Path source = root.resolve("src/main/java/example/Example.java");
            write(source, "package example;\nclass Example {}\n");
            run(root, "generateLicense", "applyLicenseHeader", "check", "--configuration-cache").build();
            assertEquals(new License(identifier, "Example Authors", "2026").renderText(),
                    read(root.resolve("LICENSE.md")));
            assertTrue(read(source).contains("SPDX-License-Identifier: " + identifier + "\n"));
        }
    }

    @Test
    public void preservesInheritedMitNoticesDuringGenerationAndCheck() throws IOException {
        Path root = fixture();
        String inherited = new License("MIT", "Example Authors", "2026").renderText()
                .replace("MIT License\n\n", "MIT License\n\nCopyright (c) 2025 Upstream Authors\n");
        write(root.resolve("LICENSE.md"), inherited);
        run(root, "generateLicense", "checkLicense").build();
        assertEquals(inherited, read(root.resolve("LICENSE.md")));
    }

    @Test
    public void preflightsAllHeadersBeforeWriting() throws IOException {
        Path root = fixture();
        Path fresh = root.resolve("src/main/java/example/Fresh.java");
        Path old = root.resolve("src/main/java/example/Old.java");
        write(fresh, "package example;\nclass Fresh {}\n");
        write(old, "/* Copyright (c) 2020 Original Author */\npackage example;\nclass Old {}\n");
        BuildResult failed = run(root, "applyLicenseHeader").buildAndFail();
        assertTrue(failed.getOutput().contains("different license notice"));
        assertEquals("package example;\nclass Fresh {}\n", read(fresh));
        assertTrue(read(old).startsWith("/* Copyright (c) 2020 Original Author */"));
    }

    private Path fixture() throws IOException {
        Path root = temporary.newFolder().toPath();
        write(root.resolve("settings.gradle.kts"), "rootProject.name = \"fixture\"\n");
        write(root.resolve("build.gradle.kts"), "plugins { java; id(\"com.sumirelabs.felt\") }\n");
        write(root.resolve("gradle.properties"),
                "license=MIT\nlicense_author=Example Authors\nlicense_year=2026\n");
        return root;
    }

    private static GradleRunner run(Path root, String... tasks) {
        return GradleRunner.create().withProjectDir(root.toFile()).withPluginClasspath().withArguments(tasks);
    }

    private static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static void write(Path path, String text) throws IOException {
        Files.createDirectories(path.getParent());
        Files.write(path, text.getBytes(StandardCharsets.UTF_8));
    }
}
