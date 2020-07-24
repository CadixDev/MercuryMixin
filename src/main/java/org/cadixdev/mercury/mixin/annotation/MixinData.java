/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.annotation;

import static org.cadixdev.mercury.mixin.util.MixinConstants.MIXIN_CLASS;

import org.cadixdev.mercury.Mercury;
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
        ITypeBinding[] targets = {};
        String[] privateTargets = {};

        for (final IMemberValuePairBinding pair : binding.getDeclaredMemberValuePairs()) {
            if (Objects.equals("value", pair.getName())) {
                final Object[] targetsTemp = (Object[]) pair.getValue();

                targets = new ITypeBinding[targetsTemp.length];
                for (int i = 0; i < targetsTemp.length; i++) {
                    targets[i] = (ITypeBinding) targetsTemp[i];
                }

            }
            if (Objects.equals("targets", pair.getName())) {
                final Object[] privateTargetsTemp = (Object[]) pair.getValue();

                privateTargets = new String[privateTargetsTemp.length];
                for (int i = 0; i < privateTargetsTemp.length; i++) {
                    privateTargets[i] = (String) privateTargetsTemp[i];
                }
            }
        }
        return new MixinData(targets, privateTargets);
    }

    protected final ITypeBinding[] targets;
    protected final String[] privateTargets;

    public MixinData(final ITypeBinding[] targets, final String[] privateTargets) {
        this.targets = targets;
        this.privateTargets = privateTargets;
    }

    /**
     * Gets <em>all</em> targets of the mixin,
     * {@link Mercury#createTypeBinding(String) creating type bindings} for
     * private targets.
     *
     * @param mercury The mercury instance
     * @return The targets
     */
    public ITypeBinding[] getTargets(final Mercury mercury) {
        final ITypeBinding[] targets = new ITypeBinding[this.targets.length + this.privateTargets.length];

        System.arraycopy(this.targets, 0, targets, 0, this.targets.length);

        for (int i = 0; i < this.privateTargets.length; i++) {
            targets[this.targets.length + i] = mercury.createTypeBinding(this.privateTargets[i])
                    .orElse(null);
        }

        return targets;
    }

    /**
     * Gets the <em>public</em> targets of the mixin.
     *
     * @return The public targets
     */
    public ITypeBinding[] getPublicTargets() {
        return this.targets;
    }

    /**
     * Gets the <em>private</em> targets of the mixin.
     *
     * @return The private targets
     */
    public String[] getPrivateTargets() {
        return this.privateTargets;
    }

}
