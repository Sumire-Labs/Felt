/*
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: BlueOak-1.0.0
 */

package com.sumirelabs.felt.gradle;

import com.sumirelabs.felt.JavaHeaders;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

@DisableCachingByDefault(because = "Edits source files in place only when explicitly requested.")
public abstract class ApplyLicenseHeader extends LicenseTask {

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getHeaderFile();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getSources();

    @TaskAction
    public void apply() throws IOException {
        String header = HeaderFiles.read(getHeaderFile().get().getAsFile(), getLicense().get().trim());
        Map<File, String> updates = new LinkedHashMap<>();
        for (File file : getSources()) {
            String original = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            String updated;
            try {
                updated = JavaHeaders.apply(original, header);
            } catch (IllegalArgumentException exception) {
                throw new GradleException(file + ": " + exception.getMessage(), exception);
            }
            if (!original.equals(updated)) {
                updates.put(file, updated);
            }
        }
        for (Map.Entry<File, String> update : updates.entrySet()) {
            Files.write(update.getKey().toPath(), update.getValue().getBytes(StandardCharsets.UTF_8));
        }
        getLogger().lifecycle("Added license headers to {} file(s).", updates.size());
    }
}
