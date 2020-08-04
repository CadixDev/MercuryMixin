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
 * A container for data held in the {@code @Slice} annotation.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public class SliceData {

    // @Slice(from = @At(...), to = @At(...))
    public static SliceData from(final IAnnotationBinding binding) {
        AtData from = null;
        AtData to = null;

        for (final IMemberValuePairBinding pair : binding.getDeclaredMemberValuePairs()) {
            if (Objects.equals("from", pair.getName())) {
                from = AtData.from((IAnnotationBinding) pair.getValue());
            }
            if (Objects.equals("to", pair.getName())) {
                to = AtData.from((IAnnotationBinding) pair.getValue());
            }
        }

        return new SliceData(from, to);
    }

    private final AtData from;
    private final AtData to;

    public SliceData(final AtData from, final AtData to) {
        this.from = from;
        this.to = to;
    }

    public AtData getFrom() {
        return this.from;
    }

    public AtData getTo() {
        return this.to;
    }

    @Override
    public String toString() {
        return "SliceData{" +
                "from=" + this.from +
                ", to=" + this.to +
                '}';
    }

}
