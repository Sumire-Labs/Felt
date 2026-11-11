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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gradle.api.GradleException;

final class HeaderFiles {

    private static final Pattern SPDX = Pattern.compile(
            "(?m)^\\s*(?:\\*|//)?\\s*SPDX-License-Identifier:\\s*(.*?)\\s*$");

    private HeaderFiles() {
    }

    static String read(File file, String identifier) throws IOException {
        try {
            String header = JavaHeaders.validate(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
            Matcher matcher = SPDX.matcher(header);
            while (matcher.find()) {
                if (!identifier.equals(matcher.group(1))) {
                    throw new IllegalArgumentException("HEADER's SPDX identifier does not match license="
                            + identifier + ". Review the header manually.");
                }
            }
            return header;
        } catch (IllegalArgumentException exception) {
            throw new GradleException(file + ": " + exception.getMessage(), exception);
        }
    }
}
