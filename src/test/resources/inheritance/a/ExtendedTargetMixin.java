/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// There are no explicit mappings for ExtendedTarget, the intention here is to
// check whether MercuryMixin will correctly pull inheritance data from parent
// classes.
@Mixin(ExtendedTarget.class)
public abstract class ExtendedTargetMixin {

    @Shadow
    public abstract void gyhu();

}
