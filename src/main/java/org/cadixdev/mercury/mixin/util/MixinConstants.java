/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.util;

public final class MixinConstants {

    public static final String MIXIN_PACKAGE = "org.spongepowered.asm.mixin";
    public static final String INJECTOR_PACKAGE = MIXIN_PACKAGE + ".injection";
    public static final String GEN_PACKAGE = MIXIN_PACKAGE + ".gen";

    public static final String MIXIN_CLASS = MIXIN_PACKAGE + ".Mixin";
    public static final String SHADOW_CLASS = MIXIN_PACKAGE + ".Shadow";
    public static final String OVERWRITE_CLASS = MIXIN_PACKAGE + ".Overwrite";
    public static final String FINAL_CLASS = MIXIN_PACKAGE + ".Final";
    public static final String MUTABLE_CLASS = MIXIN_PACKAGE + ".Mutable";
    public static final String INJECT_CLASS = INJECTOR_PACKAGE + ".Inject";
    public static final String AT_CLASS = INJECT_CLASS + ".At";
    public static final String ACCESSOR_CLASS = GEN_PACKAGE + ".Accessor";

    private MixinConstants() {
    }

}
