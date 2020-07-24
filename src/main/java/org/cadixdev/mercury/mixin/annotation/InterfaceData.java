/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.annotation;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.Objects;

/**
 * A container for data held in the {@code @Interface} annotation.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public class InterfaceData {

    // @Interface(iface=Example.class, prefix="example$")
    public static InterfaceData from(final IAnnotationBinding binding) {
        ITypeBinding iface = null;
        String prefix = "";

        for (final IMemberValuePairBinding pair : binding.getDeclaredMemberValuePairs()) {
            if (Objects.equals("iface", pair.getName())) {
                iface = (ITypeBinding) pair.getValue();
            }
            if (Objects.equals("prefix", pair.getName())) {
                prefix = (String) pair.getValue();
            }
        }

        return new InterfaceData(iface, prefix);
    }

    private final ITypeBinding iface;
    private final String prefix;

    public InterfaceData(final ITypeBinding iface, final String prefix) {
        this.iface = iface;
        this.prefix = prefix;
    }

    public ITypeBinding getIface() {
        return this.iface;
    }

    public String getPrefix() {
        return this.prefix;
    }

}
