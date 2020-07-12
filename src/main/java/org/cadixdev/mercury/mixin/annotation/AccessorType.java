/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.annotation;

import org.cadixdev.bombe.type.VoidType;
import org.cadixdev.bombe.type.signature.MethodSignature;
import org.eclipse.jdt.core.dom.IMethodBinding;

import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * The type of accessor.
 *
 * @author Adam Mummery-Smith
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public enum AccessorType {

    /**
     * A field getter, accessor must accept no args and return field type
     */
    FIELD_GETTER("get", "is"),

    /**
     * A field setter, accessor must accept single arg of the field type and
     * return void
     */
    FIELD_SETTER("set"),

    /**
     * An invoker (proxy) method
     */
    METHOD_PROXY("call", "invoke"),

    /**
     * An invoker (proxy) method
     */
    OBJECT_FACTORY("new", "create"),

    ;

    private final String[] prefixes;

    AccessorType(final String... prefixes) {
        this.prefixes = prefixes;
    }

    /**
     * Gets all of the expected prefixes for this accessor type.
     *
     * @return The expected prefixes
     */
    public String[] getPrefixes() {
        return this.prefixes;
    }

    /**
     * Checks if the given prefix is a expected for this accessor type.
     *
     * @param prefix The prefix to check
     * @return {@code true} if the prefix is expected for this type
     */
    public boolean isExpectedPrefix(final String prefix) {
        for (final String expectedPrefix : this.prefixes) {
            if (Objects.equals(expectedPrefix, prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the accessor type for the given accessor (or invoker).
     *
     * @param isInvoker Whether the accessor is a invoker
     * @param binding   The accessor method binding
     * @param signature The accessor method signature (name and descriptor)
     * @param data      The accessor data
     * @return The type of the accessor
     */
    public static AccessorType get(final boolean isInvoker, final IMethodBinding binding, final MethodSignature signature, final AccessorData data) {
        // @Invoker
        if (isInvoker) {
            if (Modifier.isStatic(binding.getModifiers())) {
                // Okay, this should be more indepth - but its enough for now
                return OBJECT_FACTORY;
            }

            return METHOD_PROXY;
        }
        // @Accessor
        else {
            return Objects.equals(VoidType.INSTANCE, signature.getDescriptor().getReturnType()) ?
                    FIELD_SETTER :
                    FIELD_GETTER;
        }
    }

}
