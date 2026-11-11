/*
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: BlueOak-1.0.0
 */

package com.sumirelabs.felt;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.junit.Test;
import static org.junit.Assert.*;

public class BuiltInLicensesTest {

    @Test
    public void rendersEverySupportedIdentifierAndRoundTripsHeaders() {
        assertEquals(11, License.supportedIdentifiers().size());
        for (String identifier : License.supportedIdentifiers()) {
            License license = new License(identifier, "Example Authors", "2026");
            String text = license.renderText();
            if (identifier.equals("MIT")) {
                assertTrue(text.contains("Copyright (c) 2026 Example Authors"));
            } else {
                assertFalse(text.contains("Copyright (c) 2026 Example Authors"));
                assertFalse(text.startsWith("SPDX-License-Identifier:"));
            }
            assertTrue(identifier, text.endsWith("\n"));
            assertTrue(identifier, license.matchesText(text));
            assertTrue(identifier, license.matchesText(text.replace("\n", "\r\n")));
            assertFalse(identifier, license.matchesText(text + "Additional condition\n"));
            String source = JavaHeaders.apply("package example;\nclass Example {}\n", license);
            assertTrue(identifier, source.contains("SPDX-License-Identifier: " + identifier + "\n"));
            assertTrue(identifier, JavaHeaders.hasHeader(source, license));
            assertEquals(source, JavaHeaders.apply(source, license));
            assertFalse(license.getName().isEmpty());
            assertEquals("https://spdx.org/licenses/" + identifier + ".html", license.getUrl());
            LicenseMetadata metadata = LicenseMetadata.forIdentifier(identifier);
            assertEquals(license.getIdentifier(), metadata.getIdentifier());
            assertEquals(license.getName(), metadata.getName());
            assertEquals(license.getUrl(), metadata.getUrl());
        }
    }

    @Test
    public void distinguishesOnlyAndOrLaterWithoutChangingTheLicenseDocument() {
        for (String version : new String[] {"GPL-2.0", "GPL-3.0", "LGPL-2.1", "LGPL-3.0"}) {
            License only = new License(version + "-only", "Example Authors", "2026");
            License later = new License(version + "-or-later", "Example Authors", "2026");
            assertEquals(only.renderText(), later.renderText());
            assertFalse(JavaHeaders.hasHeader(only.renderJavaHeader(), later));
            try {
                JavaHeaders.apply(only.renderJavaHeader() + "class Example {}", later);
                fail("Must not silently relicense an existing source file.");
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().contains("different license notice"));
            }
        }
    }

    @Test
    public void includesGplTermsExactlyOnceInLgpl3() {
        String text = new License("LGPL-3.0-only", "Example Authors", "2026").renderText();
        assertTrue(text.contains("GNU LESSER GENERAL PUBLIC LICENSE"));
        assertTrue(text.contains("GNU GENERAL PUBLIC LICENSE"));
        assertEquals(text.indexOf("GNU GENERAL PUBLIC LICENSE"), text.lastIndexOf("GNU GENERAL PUBLIC LICENSE"));
        assertTrue(text.contains("Copyright (C) 2007 Free Software Foundation, Inc."));
    }

    @Test
    public void retainsPinnedSpdxTextsVerbatim() throws Exception {
        String[][] resources = {
                {"Apache-2.0", "074e6e32c86a4c0ef8b3ed25b721ca23aca83df277cd88106ef7177c354615ff"},
                {"GPL-2.0-only", "aaf135472f81c5b4a0dca9367e5bb5e9750032b5bebe5442b36e4c0a47430df3"},
                {"GPL-3.0-only", "fb981668c18a279e285fc4d83fba1e836cc84dd4daa73c9697d3cfd2d8aca6e0"},
                {"LGPL-2.1-only", "5749785c8bdefafcb5d798270ed0a967036fe2ca63dcedade1627565dfef81d2"},
                {"LGPL-3.0-only", "996af0513df21f7496288951c41428a03c174e9e4a9d63665c57d670f845ccb1"},
                {"BlueOak-1.0.0", "8a1af140fdfbf5afd3df27f7e662f989c5b963a300020dfafce42033cae9e004"}
        };
        for (String[] resource : resources) {
            byte[] bytes;
            try (InputStream input = License.class.getResourceAsStream(
                    "/com/sumirelabs/felt/licenses/" + resource[0] + ".txt")) {
                assertNotNull(input);
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }
                bytes = output.toByteArray();
            }
            StringBuilder digest = new StringBuilder();
            for (byte value : MessageDigest.getInstance("SHA-256").digest(bytes)) {
                digest.append(String.format("%02x", value & 0xff));
            }
            assertEquals(resource[0], resource[1], digest.toString());
            String rendered = new License(resource[0], "Example Authors", "2026").renderText();
            assertEquals(new String(bytes, StandardCharsets.UTF_8), rendered);
        }
    }

    @Test
    public void rejectsAmbiguousGnuIdentifiers() {
        for (String identifier : new String[] {"GPL", "LGPL", "GPL-3.0", "LGPL-3.0"}) {
            try {
                new License(identifier, "Example Authors", "2026");
                fail("Expected an explicit version and only/or-later choice.");
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().contains("GPL-3.0-only"));
                assertTrue(expected.getMessage().contains("GPL-3.0-or-later"));
            }
        }
    }
}
