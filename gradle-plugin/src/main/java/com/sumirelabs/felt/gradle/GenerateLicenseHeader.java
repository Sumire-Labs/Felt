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
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

@DisableCachingByDefault(because = "Creates a user-maintained header file without replacing existing content.")
public abstract class GenerateLicenseHeader extends LicenseTask {

    @OutputFile
    public abstract RegularFileProperty getHeaderFile();

    @TaskAction
    public void generate() throws IOException {
        License selected = license();
        Path path = getHeaderFile().get().getAsFile().toPath();
        if (Files.exists(path)) {
            HeaderFiles.read(path.toFile(), selected.getIdentifier());
            return;
        }
        Files.createDirectories(path.toAbsolutePath().getParent());
        Files.write(path, selected.renderJavaHeader().getBytes(StandardCharsets.UTF_8));
    }
}
