/*
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: BlueOak-1.0.0
 */

package com.sumirelabs.felt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/** A license and the copyright notice used to render its text. */
public final class License {

    private final BuiltInLicense type;
    private final String author;
    private final String year;

    public License(String identifier, String author, String year) {
        this.type = BuiltInLicense.find(singleLine(identifier, "identifier"));
        this.author = singleLine(author, "author");
        this.year = singleLine(year, "year");
        if (!this.year.matches("[0-9]{4}(-[0-9]{4})?")) {
            throw new IllegalArgumentException("Year must be YYYY or YYYY-YYYY.");
        }
        if (this.year.length() == 9
                && this.year.substring(0, 4).compareTo(this.year.substring(5)) > 0) {
            throw new IllegalArgumentException("Copyright year range is reversed.");
        }
        if (this.author.contains("*/")) {
            throw new IllegalArgumentException("Author must not contain a block comment terminator.");
        }
    }

    public String getIdentifier() {
        return type.identifier;
    }

    public String getName() {
        return type.displayName;
    }

    public String getUrl() {
        return "https://spdx.org/licenses/" + type.identifier + ".html";
    }

    public static List<String> supportedIdentifiers() {
        return BuiltInLicense.identifiers();
    }

    public String renderText() {
        String text = readTemplate(type.resourceName);
        if (type == BuiltInLicense.MIT) {
            return text.replace("${year}", year).replace("${author}", author);
        }
        return text;
    }

    /** Accepts the selected license text, retaining additional MIT copyright notices. */
    public boolean matchesText(String text) {
        String actual = JavaHeaders.normalize(Objects.requireNonNull(text, "text"));
        String expected = renderText();
        if (expected.equals(actual)) {
            return true;
        }
        if (type != BuiltInLicense.MIT) {
            return false;
        }
        String prefix = "MIT License\n\n";
        int termsIndex = expected.indexOf("\n\nPermission is hereby granted");
        String terms = expected.substring(termsIndex);
        if (!actual.startsWith(prefix) || !actual.endsWith(terms)) {
            return false;
        }
        int noticesEnd = actual.length() - terms.length();
        if (noticesEnd <= prefix.length()) {
            return false;
        }
        String[] notices = actual.substring(prefix.length(), noticesEnd).split("\n", -1);
        String ownNotice = "Copyright (c) " + year + " " + author;
        boolean found = false;
        for (String notice : notices) {
            if (!notice.matches("Copyright \\(c\\) [0-9]{4}(-[0-9]{4})? .+")) {
                return false;
            }
            found |= ownNotice.equals(notice);
        }
        return found;
    }

    private static String readTemplate(String resourceName) {
        String path = "/com/sumirelabs/felt/licenses/" + resourceName + ".txt";
        try (InputStream input = License.class.getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException("Missing license template: " + resourceName);
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read license template: " + resourceName, exception);
        }
    }

    public String renderJavaHeader() {
        return "/*\n"
                + " * Copyright (c) " + year + " " + author + "\n"
                + " * SPDX-License-Identifier: " + type.identifier + "\n"
                + " */\n";
    }

    private static String singleLine(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.trim().isEmpty() || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            throw new IllegalArgumentException(name + " must be a nonempty single line.");
        }
        return value.trim();
    }
}
