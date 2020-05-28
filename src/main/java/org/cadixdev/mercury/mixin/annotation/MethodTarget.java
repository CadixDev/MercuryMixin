/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.annotation;

import org.cadixdev.bombe.type.MethodDescriptor;

import java.util.Optional;

/**
 * Method target can either be a method name or a method signature
 *
 * @author Jadon Fowler
 */
public class MethodTarget {

    private final String methodName;
    private final MethodDescriptor methodDescriptor;

    public MethodTarget(final String methodName) {
        this.methodName = methodName;
        this.methodDescriptor = null;
    }

    public MethodTarget(final String methodName, final MethodDescriptor methodDescriptor) {
        this.methodName = methodName;
        this.methodDescriptor = methodDescriptor;
    }

    public static MethodTarget of(final String target) {
        int index = target.indexOf('(');
        if (index >= 0) {
            return new MethodTarget(target.substring(0, index), MethodDescriptor.of(target.substring(index)));
        }
        return new MethodTarget(target);
    }

    public String getMethodName() {
        return this.methodName;
    }

    public Optional<MethodDescriptor> getMethodDescriptor() {
        return Optional.ofNullable(this.methodDescriptor);
    }

    @Override
    public String toString() {
        return "MethodTarget{" +
                "methodName='" + this.methodName + '\'' +
                ", methodDescriptor=" + this.methodDescriptor +
                '}';
    }

}
