/*
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: BlueOak-1.0.0
 */

package com.sumirelabs.felt.gradle;

import com.sumirelabs.felt.LicenseMetadata;
import java.io.Serializable;
import org.gradle.api.Action;
import org.gradle.api.XmlProvider;
import org.gradle.api.provider.Provider;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

final class PomLicenseAction implements Action<XmlProvider>, Serializable {

    private static final long serialVersionUID = 1L;

    private final Provider<String> identifier;

    PomLicenseAction(Provider<String> identifier) {
        this.identifier = identifier;
    }

    @Override
    public void execute(XmlProvider xml) {
        LicenseMetadata metadata = LicenseMetadata.forIdentifier(identifier.get());
        Element pom = xml.asElement();
        Element licenses = element(pom, "licenses");
        Element license = element(pom, "license");
        licenses.appendChild(license);
        appendText(license, "name", metadata.getName());
        appendText(license, "url", metadata.getUrl());
        appendText(license, "distribution", "repo");

        // POMs may already declare a license. Replace it instead of adding another.
        Node first = null;
        NodeList children = pom.getChildNodes();
        for (int index = children.getLength() - 1; index >= 0; index--) {
            Node child = children.item(index);
            String name = child.getLocalName() == null ? child.getNodeName() : child.getLocalName();
            if (child instanceof Element && "licenses".equals(name)) {
                if (first != null) {
                    pom.removeChild(first);
                }
                first = child;
            }
        }
        if (first == null) {
            pom.appendChild(licenses);
        } else {
            pom.replaceChild(licenses, first);
        }
    }

    private static Element element(Element parent, String name) {
        String namespace = parent.getNamespaceURI();
        if (namespace == null || namespace.isEmpty()) {
            namespace = parent.getOwnerDocument().getDocumentElement().getAttribute("xmlns");
        }
        return parent.getOwnerDocument().createElementNS(namespace, name);
    }

    private static void appendText(Element parent, String name, String value) {
        Element child = element(parent, name);
        child.setTextContent(value);
        parent.appendChild(child);
    }
}
