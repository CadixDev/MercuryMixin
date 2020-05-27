/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.test;

import org.junit.jupiter.api.Test;

public class MercuryMixinTests {

    @Test
    void testAccessors() throws Exception {
        new TestGroup("accessor")
                .register("TestTargetAccessor", "TestTargetAccessor")
                .test();
    }

    @Test
    void testMixins() throws Exception {
        new TestGroup("mixin")
                .register("TestTargetMixin", "TestTargetMixin")
                .test();
    }

}
