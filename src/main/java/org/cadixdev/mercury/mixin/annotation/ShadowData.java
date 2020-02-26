/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.annotation;

import static org.cadixdev.mercury.mixin.util.MixinConstants.SHADOW_CLASS;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;

import java.util.Objects;

/**
 * A container for data held in the {@code @Shadow} annotation.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public class ShadowData {

    public static ShadowData fetch(final IBinding binding) {
        for (final IAnnotationBinding annotation : binding.getAnnotations()) {
            // @Shadow(prefix="shadow$")
            if (Objects.equals(SHADOW_CLASS, annotation.getAnnotationType().getBinaryName())) {
                String prefix = "shadow$";

                for (final IMemberValuePairBinding pair : annotation.getDeclaredMemberValuePairs()) {
                    if (Objects.equals("prefix", pair.getName())) {
                        prefix = (String) pair.getValue();
                    }
                }

                return new ShadowData(prefix);
            }
        }

        return null;
    }

    private final String prefix;

    public ShadowData(final String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return this.prefix;
    }

}
