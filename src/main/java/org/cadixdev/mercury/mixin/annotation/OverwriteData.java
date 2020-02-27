/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.annotation;

import org.eclipse.jdt.core.dom.IAnnotationBinding;

/**
 * A container for data held in the {@code @Overwrite} annotation.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
// todo: is this class even needed?
public class OverwriteData {

    // @Overwrite
    public static OverwriteData from(final IAnnotationBinding binding) {
        return new OverwriteData();
    }

}
