/*
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: BlueOak-1.0.0
 */

package com.sumirelabs.felt.gradle;

import com.sumirelabs.felt.License;
import com.sumirelabs.felt.LicenseMetadata;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import static org.junit.Assert.*;

public class PomLicenseTest {

    private static final String MAVEN_NAMESPACE = "http://maven.apache.org/POM/4.0.0";

    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void replacesHardcodedLicensesAcrossAllPublicationsAndTracksChanges() throws Exception {
        Path root = fixture(true);
        for (String identifier : License.supportedIdentifiers()) {
            run(root, identifier).build();
            assertLicense(root, "main", identifier);
            assertLicense(root, "secondary", identifier);
        }
        BuildResult reused = run(root, "BlueOak-1.0.0").build();
        assertTrue(reused.getOutput().contains("Reusing configuration cache."));
        assertLicense(root, "main", "BlueOak-1.0.0");
    }

    @Test
    public void worksWhenPublishingIsAppliedBeforeFeltAndWithExtensionOverrides() throws Exception {
        Path root = fixture(false);
        Files.write(root.resolve("build.gradle.kts"),
                "\nfelt { license = \"Apache-2.0\" }\n".getBytes(StandardCharsets.UTF_8),
                java.nio.file.StandardOpenOption.APPEND);
        run(root, "MIT").build();
        assertLicense(root, "main", "Apache-2.0");
        assertLicense(root, "secondary", "Apache-2.0");
    }

    @Test
    public void rejectsUnsupportedLicenseDuringPomGeneration() throws IOException {
        Path root = fixture(true);
        BuildResult result = run(root, "UNKNOWN").buildAndFail();
        assertTrue(result.getOutput().contains("Unsupported license: UNKNOWN"));
    }

    private Path fixture(boolean feltFirst) throws IOException {
        Path root = temporary.newFolder().toPath();
        write(root.resolve("settings.gradle.kts"), "rootProject.name = \"pom-fixture\"\n");
        String plugins = feltFirst
                ? "id(\"com.sumirelabs.felt\"); `maven-publish`"
                : "`maven-publish`; id(\"com.sumirelabs.felt\")";
        write(root.resolve("build.gradle.kts"),
                "plugins { java; " + plugins + " }\n"
                + "group = \"example\"\nversion = \"1.0\"\n"
                + "publishing { publications {\n"
                + "  create<MavenPublication>(\"main\") {\n"
                + "    from(components[\"java\"])\n"
                + "    pom {\n"
                + "      name = \"Example Mod\"\n"
                + "      description = \"Keep this description\"\n"
                + "      licenses {\n"
                + "        license { name = \"MIT License\"; url = \"https://opensource.org/license/mit\" }\n"
                + "        license { name = \"Stale second license\" }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "  create<MavenPublication>(\"secondary\") {\n"
                + "    from(components[\"java\"])\n"
                + "    pom { name = \"Example Mod\"; description = \"Keep this description\" }\n"
                + "  }\n"
                + "} }\n");
        return root;
    }

    private static GradleRunner run(Path root, String identifier) {
        return GradleRunner.create().withProjectDir(root.toFile()).withPluginClasspath().withArguments(
                "generatePomFileForMainPublication", "generatePomFileForSecondaryPublication",
                "-Plicense=" + identifier, "--configuration-cache", "--rerun-tasks");
    }

    private static void assertLicense(Path root, String publication, String identifier) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document pom = factory.newDocumentBuilder().parse(
                root.resolve("build/publications/" + publication + "/pom-default.xml").toFile());
        NodeList licenses = pom.getElementsByTagNameNS(MAVEN_NAMESPACE, "licenses");
        assertEquals(1, licenses.getLength());
        NodeList entries = ((Element) licenses.item(0)).getElementsByTagNameNS(MAVEN_NAMESPACE, "license");
        assertEquals(1, entries.getLength());
        Element license = (Element) entries.item(0);
        LicenseMetadata metadata = LicenseMetadata.forIdentifier(identifier);
        assertEquals(metadata.getName(), text(license, "name"));
        assertEquals(metadata.getUrl(), text(license, "url"));
        assertEquals("repo", text(license, "distribution"));
        assertEquals("Example Mod", text(pom.getDocumentElement(), "name"));
        assertEquals("Keep this description", text(pom.getDocumentElement(), "description"));
        assertEquals("example", text(pom.getDocumentElement(), "groupId"));
    }

    private static String text(Element parent, String name) {
        return parent.getElementsByTagNameNS(MAVEN_NAMESPACE, name).item(0).getTextContent();
    }

    private static void write(Path path, String text) throws IOException {
        Files.write(path, text.getBytes(StandardCharsets.UTF_8));
    }
}
