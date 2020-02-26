/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin;

import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.mercury.RewriteContext;
import org.cadixdev.mercury.SourceRewriter;

import java.util.Objects;

public final class MixinRemapper implements SourceRewriter {

    public static SourceRewriter create(MappingSet mappings) {
        return new MixinRemapper(mappings);
    }

    private final MappingSet mappings;

    private MixinRemapper(final MappingSet mappings) {
        this.mappings = Objects.requireNonNull(mappings, "mappings");
    }

    @Override
    public int getFlags() {
        return FLAG_RESOLVE_BINDINGS;
    }

    @Override
    public void rewrite(final RewriteContext context) {
        context.getCompilationUnit().accept(new MixinRemapperVisitor(context, this.mappings));
        // todo: use the remapper visitor too?
    }

}
