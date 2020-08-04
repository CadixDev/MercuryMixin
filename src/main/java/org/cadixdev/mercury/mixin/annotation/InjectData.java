/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.annotation;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;

import java.util.Arrays;
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
        InjectTarget[] injectTargets = {};
        AtData[] atData = {};
        SliceData[] sliceData = {};

        for (final IMemberValuePairBinding pair : binding.getDeclaredMemberValuePairs()) {
            if (Objects.equals("method", pair.getName())) {
                final Object[] raw = (Object[]) pair.getValue();

                injectTargets = new InjectTarget[raw.length];
                for (int i = 0; i < raw.length; i++) {
                    injectTargets[i] = InjectTarget.of((String) raw[i]);
                }
            }
            else if (Objects.equals("at", pair.getName())) {
                final Object value = pair.getValue();

                if (value instanceof Object[]) {
                    // Injects can have an array of @At
                    final Object[] raw = (Object[]) value;

                    atData = new AtData[raw.length];
                    for (int i = 0; i < raw.length; i++) {
                        atData[i] = AtData.from((IAnnotationBinding) raw[i]);
                    }
                }
                else if (value instanceof IAnnotationBinding) {
                    // Redirects are only allowed one @At
                    atData = new AtData[]{AtData.from((IAnnotationBinding) value)};
                }
            }
            else if (Objects.equals("slice", pair.getName())) {
                final Object value = pair.getValue();

                if (value instanceof Object[]) {
                    // Injects can have an array of @Slice
                    final Object[] raw = (Object[]) value;

                    sliceData = new SliceData[raw.length];
                    for (int i = 0; i < raw.length; i++) {
                        sliceData[i] = SliceData.from((IAnnotationBinding) raw[i]);
                    }
                }
                else if (value instanceof IAnnotationBinding) {
                    // Redirects are only allowed one @At
                    sliceData = new SliceData[]{SliceData.from((IAnnotationBinding) value)};
                }
            }
        }

        return new InjectData(injectTargets, atData, sliceData);
    }

    private final InjectTarget[] injectTargets;
    private final AtData[] atData;
    private final SliceData[] sliceData;

    public InjectData(final InjectTarget[] injectTargets, final AtData[] atData, final SliceData[] sliceData) {
        this.injectTargets = injectTargets;
        this.atData = atData;
        this.sliceData = sliceData;
    }

    public InjectTarget[] getInjectTargets() {
        return this.injectTargets;
    }

    public AtData[] getAtData() {
        return this.atData;
    }

    public SliceData[] getSliceData() {
        return this.sliceData;
    }

    @Override
    public String toString() {
        return "InjectData{" +
                "methodTargets=" + Arrays.toString(this.injectTargets) +
                ", atData=" + Arrays.toString(this.atData) +
                ", sliceData=" + Arrays.toString(this.sliceData) +
                '}';
    }

}
