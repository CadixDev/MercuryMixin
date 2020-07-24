/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.annotation;

import static org.cadixdev.mercury.mixin.util.MixinConstants.IMPLEMENTS_CLASS;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.Objects;

/**
 * A container for data held in the {@code @Implements} annotation.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public class ImplementsData {

    public static ImplementsData fetch(final ITypeBinding binding) {
        for (final IAnnotationBinding annotation : binding.getAnnotations()) {
            if (Objects.equals(IMPLEMENTS_CLASS, annotation.getAnnotationType().getBinaryName())) {
                return from(annotation);
            }
        }

        return null;
    }

    // @Implements(@Interface(iface=Example.class, prefix="example$"))
    public static ImplementsData from(final IAnnotationBinding binding) {
        InterfaceData[] value = null;

        for (final IMemberValuePairBinding pair : binding.getDeclaredMemberValuePairs()) {
            if (Objects.equals("value", pair.getName())) {
                final Object[] temp = (Object[]) pair.getValue();

                value = new InterfaceData[temp.length];
                for (int i = 0; i < temp.length; i++) {
                    final IAnnotationBinding valueBinding = (IAnnotationBinding) temp[i];
                    value[i] = InterfaceData.from(valueBinding);
                }
            }
        }

        return new ImplementsData(value);
    }

    private final InterfaceData[] value;

    public ImplementsData(final InterfaceData[] value) {
        this.value = value;
    }

    public InterfaceData[] getValue() {
        return this.value;
    }

}
