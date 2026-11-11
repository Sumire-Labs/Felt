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
    public void rendersOneCopyrightLinePerAuthorInHeadersAndMitText() {
        License multiple = new License("MIT", " Alice , Bob, Sumire Labs ", "2024-2026");
        assertEquals("/*\n * Copyright (c) 2024-2026 Alice\n * Copyright (c) 2024-2026 Bob\n"
                + " * Copyright (c) 2024-2026 Sumire Labs\n * SPDX-License-Identifier: MIT\n */\n",
                multiple.renderJavaHeader());
        String text = multiple.renderText();
        assertTrue(text.startsWith("MIT License\n\nCopyright (c) 2024-2026 Alice\n"
                + "Copyright (c) 2024-2026 Bob\nCopyright (c) 2024-2026 Sumire Labs\n\nPermission"));
        assertTrue(multiple.matchesText(text));
        String inherited = text.replace("MIT License\n\n", "MIT License\n\nCopyright (c) 2020 Upstream Authors\n");
        assertTrue(multiple.matchesText(inherited));
        assertFalse(multiple.matchesText(inherited.replace("Copyright (c) 2024-2026 Bob\n", "")));
        assertFalse(multiple.matchesText(inherited.replace("2024-2026 Bob", "2026 Bob")));
    }

    @Test
    public void rejectsEmptyEntriesInAuthorLists() {
        for (String author : new String[] {",Alice", "Alice,", "Alice,,Bob", "Alice, ,Bob"}) {
            try {
                new License("MIT", author, "2026");
                fail("Empty author entries must not generate empty copyright notices.");
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().contains("author"));
            }
        }
    }

    @Test
    public void keepsNonMitLicenseDocumentsUnchangedWithMultipleAuthors() {
        for (String identifier : License.supportedIdentifiers()) {
            if (!identifier.equals("MIT")) {
                License multiple = new License(identifier, "Alice, Bob", "2026");
                assertEquals(new License(identifier, "Alice", "2026").renderText(), multiple.renderText());
                assertTrue(multiple.renderJavaHeader().contains("Copyright (c) 2026 Alice\n * Copyright (c) 2026 Bob\n"));
            }
        }
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
    public void appliesCustomCommentHeadersAndPreservesSourceEncoding() {
        String header = "/* Custom project notice. */\r\n// SPDX-License-Identifier: MIT\r\n";
        String source = "\uFEFFpackage example;\r\nclass Example {}\r\n";
        String updated = JavaHeaders.apply(source, header);
        assertTrue(updated.startsWith("\uFEFF" + header + "\r\n"));
        assertTrue(updated.endsWith(source.substring(1)));
        assertTrue(JavaHeaders.hasHeader(updated, header));
        assertEquals(updated, JavaHeaders.apply(updated, header));
    }

    @Test
    public void refusesEmptyHeadersAndHeadersContainingCode() {
        for (String header : new String[] {"", "   ", "/* Unclosed", "/* Comment */\nclass Injected {}"}) {
            try {
                JavaHeaders.apply("class Example {}\n", header);
                fail("Invalid HEADER must not be inserted into source files.");
            } catch (IllegalArgumentException expected) {
                assertFalse(expected.getMessage().isEmpty());
            }
        }
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
