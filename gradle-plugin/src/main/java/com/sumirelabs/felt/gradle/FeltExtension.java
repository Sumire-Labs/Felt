/*
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: BlueOak-1.0.0
 */

package com.sumirelabs.felt.gradle;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

/** License settings shared by Felt's tasks. */
public abstract class FeltExtension {

    public abstract Property<String> getLicense();

    public abstract Property<String> getAuthor();

    public abstract Property<String> getYear();

    public abstract RegularFileProperty getLicenseFile();

    public abstract RegularFileProperty getHeaderFile();

    public abstract ConfigurableFileCollection getSources();
}
