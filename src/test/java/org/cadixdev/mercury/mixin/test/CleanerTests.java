/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.test;

import org.cadixdev.mercury.mixin.cleaner.MixinCleaner;
import org.junit.jupiter.api.Test;

public class CleanerTests {

    @Test
    void testMixins() throws Exception {
        new TestGroup("cleaner", (mercury, mappings) -> {
            mercury.getProcessors().add(MixinCleaner.create());
        })
                .register("PrivateTestTargetMixin", "PrivateTestTargetMixin")
                .register("TestTargetMixin", "TestTargetMixin")
                .test();
    }

}
