/*
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: BlueOak-1.0.0
 */

package com.sumirelabs.felt;

import java.util.Objects;

/** License information for package metadata, independent of copyright settings. */
public final class LicenseMetadata {

    private final BuiltInLicense type;

    private LicenseMetadata(BuiltInLicense type) {
        this.type = type;
    }

    public static LicenseMetadata forIdentifier(String identifier) {
        return new LicenseMetadata(BuiltInLicense.find(Objects.requireNonNull(identifier, "identifier").trim()));
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
}
