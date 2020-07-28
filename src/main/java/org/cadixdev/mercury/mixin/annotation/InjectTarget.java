/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.annotation;

import org.cadixdev.bombe.type.MethodDescriptor;
import org.cadixdev.bombe.type.Type;
import org.cadixdev.bombe.type.TypeReader;

import java.util.Optional;

/**
 * Inject target can either be a name, a name and method signature, or a name and field type.
 *
 * @author Jadon Fowler
 * @since 0.1.0
 */
public class InjectTarget {

    private final String targetName;
    private final MethodDescriptor methodDescriptor;
    private final Type fieldType;

    public InjectTarget(final String targetName) {
        this.targetName = targetName;
        this.methodDescriptor = null;
        this.fieldType = null;
    }

    public InjectTarget(final String targetName, final MethodDescriptor methodDescriptor) {
        this.targetName = targetName;
        this.methodDescriptor = methodDescriptor;
        this.fieldType = null;
    }

    public InjectTarget(final String targetName, final Type fieldType) {
        this.targetName = targetName;
        this.methodDescriptor = null;
        this.fieldType = fieldType;
    }

    public static InjectTarget of(final String target) {
        int index = target.indexOf('(');
        if (index >= 0) {
            return new InjectTarget(target.substring(0, index), MethodDescriptor.of(target.substring(index)));
        }
        index = target.indexOf(':');
        if (index >= 0) {
            Type fieldType = new TypeReader(target.substring(index + 1)).readType();
            return new InjectTarget(target.substring(0, index), fieldType);
        }
        return new InjectTarget(target);
    }

    public String getTargetName() {
        return this.targetName;
    }

    public Optional<MethodDescriptor> getMethodDescriptor() {
        return Optional.ofNullable(this.methodDescriptor);
    }

    public Optional<Type> getFieldType() {
        return Optional.ofNullable(this.fieldType);
    }

    public String getFullTarget() {
        String full = targetName;

        if (methodDescriptor != null) {
            full += methodDescriptor.toString();
        } else if (fieldType != null) {
            full += ":" + fieldType.toString();
        }

        return full;
    }

    @Override
    public String toString() {
        return "InjectTarget{" +
                "targetName='" + targetName + '\'' +
                ", methodDescriptor=" + methodDescriptor +
                ", fieldType=" + fieldType +
                '}';
    }

}
