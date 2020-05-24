/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.annotation;

import org.cadixdev.bombe.type.signature.MethodSignature;
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
        MethodSignature[] methodSignatures = {};

        for (final IMemberValuePairBinding pair : binding.getDeclaredMemberValuePairs()) {
            if (Objects.equals("method", pair.getName())) {
                final Object[] raw = (Object[]) pair.getValue();

                methodSignatures = new MethodSignature[raw.length];
                for (int i = 0; i < raw.length; i++) {
                    methodSignatures[i] = MethodSignature.of((String) raw[i]);
                }
            }
        }

        return new InjectData(methodSignatures);
    }

    private final MethodSignature[] methodSignatures;

    public InjectData(final MethodSignature[] methodSignatures) {
        this.methodSignatures = methodSignatures;
    }

    public MethodSignature[] getMethodSignatures() {
        return this.methodSignatures;
    }

}
