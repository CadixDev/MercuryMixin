/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.annotation;

import static org.cadixdev.mercury.mixin.util.MixinConstants.OVERWRITE_CLASS;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;

import java.util.Objects;

/**
 * A container for data held in the {@code @Overwrite} annotation.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public class OverwriteData {

    public static OverwriteData fetch(final IBinding binding) {
        for (final IAnnotationBinding annotation : binding.getAnnotations()) {
            // @Overwrite
            if (Objects.equals(OVERWRITE_CLASS, annotation.getAnnotationType().getBinaryName())) {
                return new OverwriteData();
            }
        }

        return null;
    }

}
