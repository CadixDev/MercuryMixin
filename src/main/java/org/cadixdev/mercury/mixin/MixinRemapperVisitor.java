/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin;

import static org.cadixdev.mercury.util.BombeBindings.convertSignature;

import org.cadixdev.bombe.analysis.InheritanceProvider;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.FieldMapping;
import org.cadixdev.mercury.RewriteContext;
import org.cadixdev.mercury.analysis.MercuryInheritanceProvider;
import org.cadixdev.mercury.mixin.annotation.MixinData;
import org.cadixdev.mercury.mixin.annotation.ShadowData;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

public class MixinRemapperVisitor extends ASTVisitor {

    final RewriteContext context;
    final MappingSet mappings;
    private final InheritanceProvider inheritanceProvider;

    MixinRemapperVisitor(final RewriteContext context, final MappingSet mappings) {
        this.context = context;
        this.mappings = mappings;
        this.inheritanceProvider = MercuryInheritanceProvider.get(context.getMercury());
    }

    void remapField(final SimpleName node, final IVariableBinding binding) {
        if (!binding.isField()) return;

        final ITypeBinding declaringClass = binding.getDeclaringClass();
        final MixinData mixin = MixinData.fetch(declaringClass);
        if (mixin == null) return;

        // todo: support multiple targets properly
        final ClassMapping<?, ?> target = this.mappings.computeClassMapping(mixin.getTargets()[0].getBinaryName()).orElse(null);
        if (target == null) return;

        // Get Shadow info, if it exists
        final ShadowData shadow = ShadowData.fetch(binding);
        if (shadow != null) {
            // todo: handle prefix

            // Get mapping of target field
            final FieldMapping targetField = target.computeFieldMapping(convertSignature(binding)).orElse(null);
            if (targetField != null) {
                // Create mapping for mixin
                final ClassMapping<?, ?> classMapping = this.mappings.getOrCreateClassMapping(declaringClass.getBinaryName());
                final FieldMapping field = classMapping.getOrCreateFieldMapping(convertSignature(binding));
                field.setDeobfuscatedName(targetField.getDeobfuscatedName());
            }
        }
    }

    private void visit(final SimpleName node, final IBinding binding) {
        switch (binding.getKind()) {
            case IBinding.METHOD:
                //remapMethod(node, ((IMethodBinding) binding).getMethodDeclaration());
                break;
            case IBinding.VARIABLE:
                remapField(node, ((IVariableBinding) binding).getVariableDeclaration());
                break;
        }
    }

    @Override
    public final boolean visit(final SimpleName node) {
        final IBinding binding = node.resolveBinding();
        if (binding != null) {
            visit(node, binding);
        }
        return false;
    }

}
