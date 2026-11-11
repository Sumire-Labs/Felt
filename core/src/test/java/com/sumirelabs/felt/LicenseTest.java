/*
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: BlueOak-1.0.0
 */

package com.sumirelabs.felt;

import org.junit.Test;
import static org.junit.Assert.*;

public class LicenseTest {

    private final License license = new License("MIT", "Example Authors", "2024-2026");

    @Test
    public void rendersMitWithCopyright() {
        String text = license.renderText();
        assertTrue(text.startsWith("MIT License\n\nCopyright (c) 2024-2026 Example Authors\n"));
        assertTrue(text.contains("Permission is hereby granted, free of charge"));
        assertFalse(text.contains("${"));
    }

    @Test
    public void acceptsAdditionalMitCopyrightNoticesWithoutDroppingThem() {
        String original = license.renderText();
        String inherited = original.replace("MIT License\n\n",
                "MIT License\n\nCopyright (c) 2020 Upstream Authors\n");
        assertTrue(license.matchesText(inherited));
        assertTrue(license.matchesText(inherited.replace("\n", "\r\n")));
        assertFalse(license.matchesText(inherited.replace("free of charge", "for a fee")));
        assertFalse(license.matchesText(inherited.replace("2024-2026 Example Authors", "2026 Someone Else")));
        assertFalse(license.matchesText(inherited.replace("MIT License\n\n", "MIT License\n\nSome extra condition\n")));
    }

    @Test
    public void keepsSourceBodyAndDoesNotDuplicateHeaders() {
        String original = "/** Package documentation. */\npackage example;\n";
        String updated = JavaHeaders.apply(original, license);
        assertTrue(updated.endsWith(original));
        assertTrue(JavaHeaders.hasHeader(updated, license));
        assertEquals(updated, JavaHeaders.apply(updated, license));
    }

    @Test
    public void preservesBomAndWindowsLineEndings() {
        String original = "\uFEFFpackage example;\r\n";
        String updated = JavaHeaders.apply(original, license);
        assertTrue(updated.startsWith("\uFEFF/*\r\n"));
        assertTrue(updated.endsWith("package example;\r\n"));
        assertTrue(JavaHeaders.hasHeader(updated, license));
        assertEquals(updated, JavaHeaders.apply(updated, license));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsExistingCopyrightAfterOrdinaryComment() {
        JavaHeaders.apply("// Project details\n/* Copyright (c) 2020 Someone else */\nclass Example {}", license);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsExistingSpdxNotice() {
        JavaHeaders.apply("// SPDX-License-Identifier: Apache-2.0\nclass Example {}", license);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnknownLicense() {
        new License("UNKNOWN", "Example Authors", "2026");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsCommentInjection() {
        new License("MIT", "Example */ class Injected {} /*", "2026");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsReversedYears() {
        new License("MIT", "Example Authors", "2026-2024");
    }
}
