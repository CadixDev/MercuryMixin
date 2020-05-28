/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TestTarget.class)
public abstract class TestTargetMixin {

    @Shadow
    private String test;

    @Shadow
    @Final
    private String name;

    @Shadow
    @Mutable
    private int shadow$age;

    @Shadow
    public abstract String getName();

    @Shadow(prefix="test$")
    public abstract int test$getAge();

    @Overwrite
    public void run() {
        System.out.println("Hello, world!");
    }

    @Inject(method = {"start", "jjjj"}, at = @At("HEAD"))
    public void onStart(final CallbackInfo callbackInfo) {
        System.out.println("Hello, world!");
    }

    @Inject(method = {"start()V", "jjjj"}, at = @At("HEAD"))
    public void onStart2(final CallbackInfo callbackInfo) {
        System.out.println("Hello, world!");
    }

}
