/*
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: BlueOak-1.0.0
 */

package com.sumirelabs.felt.gradle;

import com.sumirelabs.felt.License;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.work.DisableCachingByDefault;

@DisableCachingByDefault(because = "Base type for explicit license edits and read-only checks.")
public abstract class LicenseTask extends DefaultTask {

    @Input
    public abstract Property<String> getLicense();

    @Input
    public abstract Property<String> getAuthor();

    @Input
    public abstract Property<String> getYear();

    protected final License license() {
        return new License(getLicense().get(), getAuthor().get(), getYear().get());
    }
}
