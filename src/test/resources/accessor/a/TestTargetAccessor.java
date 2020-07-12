/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(hj.class)
public interface TestTargetAccessor {

    @Accessor
    public int getYu();

    @Accessor
    public void setYu(final int age);

    @Accessor("thhh")
    public String getTest();

    @Invoker
    public void callGhy(final int year);

    @Invoker("hj")
    static hj createTestTarget(final String test) {
        return null;
    }

}
