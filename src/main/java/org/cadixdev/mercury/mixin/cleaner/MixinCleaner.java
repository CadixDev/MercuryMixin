/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.cleaner;

import org.cadixdev.mercury.RewriteContext;
import org.cadixdev.mercury.SourceRewriter;

public final class MixinCleaner implements SourceRewriter {

    public static SourceRewriter create() {
        return new MixinCleaner();
    }

    private MixinCleaner() {
    }

    @Override
    public int getFlags() {
        return FLAG_RESOLVE_BINDINGS;
    }

    @Override
    public void rewrite(final RewriteContext context) {
        context.getCompilationUnit().accept(new FinalVisitor(context));
    }

}
