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
 * A container for data held in the {@code @Inject} annotation.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public class InjectData {

    // @Inject(method={"example"}, at=@At(...))
    public static InjectData from(final IAnnotationBinding binding) {
        MethodTarget[] methodTargets = {};

        for (final IMemberValuePairBinding pair : binding.getDeclaredMemberValuePairs()) {
            if (Objects.equals("method", pair.getName())) {
                final Object[] raw = (Object[]) pair.getValue();

                methodTargets = new MethodTarget[raw.length];
                for (int i = 0; i < raw.length; i++) {
                    methodTargets[i] = MethodTarget.of((String) raw[i]);
                }
            }
        }

        return new InjectData(methodTargets);
    }

    private final MethodTarget[] methodTargets;

    public InjectData(final MethodTarget[] methodTargets) {
        this.methodTargets = methodTargets;
    }

    public MethodTarget[] getMethodTargets() {
        return this.methodTargets;
    }

}
