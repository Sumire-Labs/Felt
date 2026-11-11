/*
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: BlueOak-1.0.0
 */

package com.sumirelabs.felt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

enum BuiltInLicense {

    MIT("MIT", "MIT License", "MIT"),
    APACHE_2("Apache-2.0", "Apache License 2.0", "Apache-2.0"),
    GPL_2_ONLY("GPL-2.0-only", "GNU General Public License v2.0 only", "GPL-2.0-only"),
    GPL_2_LATER("GPL-2.0-or-later", "GNU General Public License v2.0 or later", "GPL-2.0-only"),
    GPL_3_ONLY("GPL-3.0-only", "GNU General Public License v3.0 only", "GPL-3.0-only"),
    GPL_3_LATER("GPL-3.0-or-later", "GNU General Public License v3.0 or later", "GPL-3.0-only"),
    LGPL_21_ONLY("LGPL-2.1-only", "GNU Lesser General Public License v2.1 only", "LGPL-2.1-only"),
    LGPL_21_LATER("LGPL-2.1-or-later", "GNU Lesser General Public License v2.1 or later", "LGPL-2.1-only"),
    LGPL_3_ONLY("LGPL-3.0-only", "GNU Lesser General Public License v3.0 only", "LGPL-3.0-only"),
    LGPL_3_LATER("LGPL-3.0-or-later", "GNU Lesser General Public License v3.0 or later", "LGPL-3.0-only"),
    BLUE_OAK("BlueOak-1.0.0", "Blue Oak Model License 1.0.0", "BlueOak-1.0.0");

    private static final List<String> IDENTIFIERS;

    static {
        List<String> identifiers = new ArrayList<>();
        for (BuiltInLicense license : values()) {
            identifiers.add(license.identifier);
        }
        IDENTIFIERS = Collections.unmodifiableList(identifiers);
    }

    final String identifier;
    final String displayName;
    final String resourceName;

    BuiltInLicense(String identifier, String displayName, String resourceName) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.resourceName = resourceName;
    }

    static List<String> identifiers() {
        return IDENTIFIERS;
    }

    static BuiltInLicense find(String identifier) {
        for (BuiltInLicense license : values()) {
            if (license.identifier.equals(identifier)) {
                return license;
            }
        }
        throw new IllegalArgumentException("Unsupported license: " + identifier
                + ". Use an SPDX identifier from: " + String.join(", ", IDENTIFIERS));
    }
}
