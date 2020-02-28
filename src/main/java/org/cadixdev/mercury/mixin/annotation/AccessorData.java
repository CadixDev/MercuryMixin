/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.annotation;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;

import java.util.Objects;

/**
 * A container for data held in the {@code @Accessor} annotation.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public class AccessorData {

    // @Accessor(value="target")
    public static AccessorData from(final IAnnotationBinding binding) {
        String value = "";

        for (final IMemberValuePairBinding pair : binding.getDeclaredMemberValuePairs()) {
            if (Objects.equals("value", pair.getName())) {
                value = (String) pair.getValue();
            }
        }

        return new AccessorData(value);
    }

    private final String target;

    public AccessorData(final String target) {
        this.target = target;
    }

    public String getTarget() {
        return this.target;
    }

}
