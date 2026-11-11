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
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

@DisableCachingByDefault(because = "Writes a user-maintained license file and refuses conflicting content.")
public abstract class GenerateLicense extends LicenseTask {

    @OutputFile
    public abstract RegularFileProperty getLicenseFile();

    @TaskAction
    public void generate() throws IOException {
        Path path = getLicenseFile().get().getAsFile().toPath();
        License selected = license();
        String expected = selected.renderText();
        if (Files.exists(path)) {
            String actual = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            if (!selected.matchesText(actual)) {
                throw new GradleException("License file already has different content: " + path
                        + ". Review it manually; Felt will not overwrite it.");
            }
            return;
        }
        Files.createDirectories(path.toAbsolutePath().getParent());
        Files.write(path, expected.getBytes(StandardCharsets.UTF_8));
    }
}
