/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.cadixdev.bombe.util.ByteStreams;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.io.MappingFormats;
import org.cadixdev.lorenz.io.MappingsReader;
import org.cadixdev.mercury.Mercury;
import org.cadixdev.mercury.remapper.MercuryRemapper;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class TestGroup {

    private static final Set<String> COMMON_SOURCES;

    static {
        final Set<String> commonSources = new HashSet<>();
        commonSources.add("ExtendedTarget");
        commonSources.add("hj");
        commonSources.add("InterfaceTest");
        commonSources.add("obf/OutInner");

        COMMON_SOURCES = Collections.unmodifiableSet(commonSources);
    }

    private final Map<String, String> expected = new HashMap<>();
    private final String name;
    private final BiConsumer<Mercury, MappingSet> mercuryHandler;
    private final Path dir;
    private final Path mixin;
    private final MappingSet mappings;

    public TestGroup(final String name, final BiConsumer<Mercury, MappingSet> mercury) throws IOException {
        this.name = name;
        this.mercuryHandler = mercury;

        // Create temporary directory, as Mercury needs to operate on the actual file
        // system.
        this.dir = Files.createTempDirectory("mercury-test");
        Files.createDirectories(this.dir.resolve("a"));
        Files.createDirectories(this.dir.resolve("b"));
        this.mixin = this.dir.resolve("mixin.jar");
        downloadMixin(this.mixin);

        this.mappings = MappingSet.create();
        // Read the common mappings
        try (final MappingsReader reader = MappingFormats.TSRG
                .createReader(TestGroup.class.getResourceAsStream("/common/test.tsrg"))) {
            reader.read(this.mappings);
        }
        // Read the test's mappings
        try (final MappingsReader reader = MappingFormats.TSRG
                .createReader(TestGroup.class.getResourceAsStream("/" + this.name + "/test.tsrg"))) {
            reader.read(this.mappings);
        }

        // Copy common sources to a
        for (final String file : COMMON_SOURCES) {
            this.copy("/common/src/", file);
        }
    }

    public TestGroup register(final String a, final String b) throws IOException {
        final String prefix = "/" + this.name + "/";

        // Copy to a
        this.copy(prefix + "a/", a);

        // Register test
        this.expected.put(a + ".java", b + ".java");

        return this;
    }

    public void test() throws Exception {
        final Path in = this.dir.resolve("a");
        final Path out = this.dir.resolve("b");

        final Mercury mercury = new Mercury();
        mercury.getClassPath().add(this.mixin);
        this.mercuryHandler.accept(mercury, this.mappings);
        mercury.getProcessors().add(MercuryRemapper.create(this.mappings));
        mercury.rewrite(in, out);

        for (final String file : this.expected.values()) {
            final Path path = out.resolve(file);

            // First check the path exists
            assertTrue(Files.exists(path), file + " doesn't exists!");

            // Check the file matches the expected output
            final String expected;
            try (final InputStream is = TestGroup.class.getResourceAsStream("/" + this.name + "/b/" + file)) {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ByteStreams.copy(is, baos);
                expected = baos.toString();
            }
            final String actual = new String(Files.readAllBytes(path));
            assertEquals(expected, actual, "Remapped code for " + file + " does not match expected");
        }
    }

    private void copy(final String prefix, final String file) throws IOException {
        final Path path = this.dir.resolve("a").resolve(file + ".java");

        // Make sure the parent directory exists
        Files.createDirectories(path.getParent());

        // Copy the file to the file system
        Files.copy(
                TestGroup.class.getResourceAsStream(prefix + file + ".java"),
                path,
                StandardCopyOption.REPLACE_EXISTING
        );

        // Finally verify the file exists, to prevent issues later on
        assertTrue(Files.exists(path), file + " failed to copy!");
    }

    private static void downloadMixin(final Path path) throws IOException {
        final URL url = new URL("https://repo.spongepowered.org/maven/org/spongepowered/mixin/0.8/mixin-0.8.jar");
        try (final ReadableByteChannel rbc = Channels.newChannel(url.openStream());
             final FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

}
