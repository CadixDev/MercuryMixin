/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.annotation;

import static org.cadixdev.mercury.mixin.util.MixinConstants.MIXIN_CLASS;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.Objects;

/**
 * A container for data held in the {@code @Mixin} annotation.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public class MixinData {

    public static MixinData fetch(final ITypeBinding binding) {
        for (final IAnnotationBinding annotation : binding.getAnnotations()) {
            if (Objects.equals(MIXIN_CLASS, annotation.getAnnotationType().getBinaryName())) {
                return from(annotation);
            }
        }

        return null;
    }

    // @Mixin(value = {klass.class}, targets = {"private"})
    public static MixinData from(final IAnnotationBinding binding) {
        Object[] targetsTemp = {};
        String[] privateTargets = {};

        for (final IMemberValuePairBinding pair : binding.getDeclaredMemberValuePairs()) {
            if (Objects.equals("value", pair.getName())) {
                targetsTemp = (Object[]) pair.getValue();
            }
            if (Objects.equals("targets", pair.getName())) {
                privateTargets = (String[]) pair.getValue();
            }
        }

        final ITypeBinding[] targets = new ITypeBinding[targetsTemp.length];
        for (int i = 0; i < targetsTemp.length; i++) {
            targets[i] = (ITypeBinding) targetsTemp[i];
        }

        return new MixinData(targets, privateTargets);
    }

    private final ITypeBinding[] targets;
    private final String[] privateTargets;

    public MixinData(final ITypeBinding[] targets, final String[] privateTargets) {
        this.targets = targets;
        this.privateTargets = privateTargets;
    }

    public ITypeBinding[] getTargets() {
        return this.targets;
    }

    public String[] getPrivateTargets() {
        return this.privateTargets;
    }

}
