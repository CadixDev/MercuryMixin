/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;

@Mixin(TestTarget.class)
@Implements(@Interface(iface=InterfaceTest.class, prefix="if$"))
public abstract class TestImplementsMixin {

    @Shadow
    @Final
    private String name;

    public String if$getName() {
        return this.name;
    }

}
