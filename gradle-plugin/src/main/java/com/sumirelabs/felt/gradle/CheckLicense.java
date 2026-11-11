/*
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: BlueOak-1.0.0
 */

package com.sumirelabs.felt.gradle;

import com.sumirelabs.felt.JavaHeaders;
import com.sumirelabs.felt.License;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

@DisableCachingByDefault(because = "Read-only verification without output artifacts.")
public abstract class CheckLicense extends LicenseTask {

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getLicenseFile();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getHeaderFile();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getSources();

    @TaskAction
    public void check() throws IOException {
        License selected = license();
        String header = HeaderFiles.read(getHeaderFile().get().getAsFile(), selected.getIdentifier());
        List<String> failures = new ArrayList<>();
        File licenseFile = getLicenseFile().get().getAsFile();
        String text = new String(Files.readAllBytes(licenseFile.toPath()), StandardCharsets.UTF_8);
        if (!selected.matchesText(text)) {
            failures.add("License file does not match the configured license: " + licenseFile);
        }
        for (File file : getSources()) {
            String source = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            if (!JavaHeaders.hasHeader(source, header)) {
                failures.add("Missing or different license header: " + file);
            }
        }
        if (!failures.isEmpty()) {
            throw new GradleException(String.join("\n", failures));
        }
    }
}
