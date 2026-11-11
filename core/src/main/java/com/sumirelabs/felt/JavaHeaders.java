/*
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: BlueOak-1.0.0
 */

package com.sumirelabs.felt;

import java.util.Locale;
import java.util.Objects;

/** Adds Java headers without replacing existing copyright notices. */
public final class JavaHeaders {

    private JavaHeaders() {
    }

    public static boolean hasHeader(String source, License license) {
        return normalize(withoutBom(Objects.requireNonNull(source, "source")))
                .startsWith(license.renderJavaHeader());
    }

    public static String apply(String source, License license) {
        Objects.requireNonNull(source, "source");
        if (hasHeader(source, license)) {
            return source;
        }
        String body = withoutBom(source);
        String remaining = body.trim();
        while (remaining.startsWith("/*") || remaining.startsWith("//")) {
            int end;
            if (remaining.startsWith("/*")) {
                int close = remaining.indexOf("*/", 2);
                if (close < 0) {
                    throw new IllegalArgumentException("Source starts with an unterminated comment.");
                }
                end = close + 2;
            } else {
                end = remaining.indexOf('\n');
                if (end < 0) {
                    end = remaining.length();
                }
            }
            String comment = remaining.substring(0, end).toLowerCase(Locale.ROOT);
            if (comment.contains("copyright") || comment.contains("spdx-license-identifier")
                    || comment.contains("license") || comment.contains("licence")) {
                throw new IllegalArgumentException("Source already has a different license notice; review it manually.");
            }
            remaining = remaining.substring(end).trim();
        }
        String newline = body.contains("\r\n") ? "\r\n" : "\n";
        String bom = source.startsWith("\uFEFF") ? "\uFEFF" : "";
        return bom + license.renderJavaHeader().replace("\n", newline) + newline + body;
    }

    public static String normalize(String text) {
        return text.replace("\r\n", "\n");
    }

    private static String withoutBom(String text) {
        return text.startsWith("\uFEFF") ? text.substring(1) : text;
    }
}
