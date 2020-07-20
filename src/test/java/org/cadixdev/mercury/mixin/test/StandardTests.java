/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.test;

import org.cadixdev.mercury.mixin.MixinRemapper;
import org.junit.jupiter.api.Test;

public class StandardTests {

    @Test
    void testAccessors() throws Exception {
        new TestGroup("accessor", (mercury, mappings) -> {
            mercury.getProcessors().add(MixinRemapper.create(mappings));
        })
                .register("TestTargetAccessor", "TestTargetAccessor")
                .test();
    }

    @Test
    void testMixins() throws Exception {
        new TestGroup("mixin", (mercury, mappings) -> {
            mercury.getProcessors().add(MixinRemapper.create(mappings));
        })
                .register("OtherOuterMixin", "OtherOuterMixin")
                .register("OutInner_1Mixin", "OutInner_1Mixin")
                .register("OutInner_2Mixin", "OutInner_2Mixin")
                .register("OutInnerMixin", "OutInnerMixin")
                .register("TestTargetMixin", "TestTargetMixin")
                .test();
    }

    @Test
    void testInheritance() throws Exception {
        new TestGroup("inheritance", (mercury, mappings) -> {
            mercury.getProcessors().add(MixinRemapper.create(mappings));
        })
                .register("ExtendedTargetMixin", "ExtendedTargetMixin")
                .test();
    }

}
