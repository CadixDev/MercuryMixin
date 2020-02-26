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
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

import java.util.Objects;

public class MixinRemapperVisitor extends ASTVisitor {

    final RewriteContext context;
    final MappingSet mappings;
    private final InheritanceProvider inheritanceProvider;

    MixinRemapperVisitor(final RewriteContext context, final MappingSet mappings) {
        this.context = context;
        this.mappings = mappings;
        this.inheritanceProvider = MercuryInheritanceProvider.get(context.getMercury());
    }

    private MixinInformation getMixinInfo(final ITypeBinding declaringClass) {
        boolean containsMixins = false;
        ClassMapping<?, ?> target = null;

        for (final IAnnotationBinding annotation : declaringClass.getAnnotations()) {
            if (Objects.equals("org.spongepowered.asm.mixin.Mixin", annotation.getAnnotationType().getBinaryName())) {
                containsMixins = true;

                for (final IMemberValuePairBinding pair : annotation.getDeclaredMemberValuePairs()) {
                    if ("value".equals(pair.getName())) {
                        final Object[] value = (Object[]) pair.getValue();
                        // todo: support multiple targets
                        target = this.mappings.getOrCreateClassMapping(((ITypeBinding) value[0]).getBinaryName());
                    }
                    // eventually store prefix, etc...
                }
            }
        }

        return new MixinInformation(containsMixins, target);
    }

    void remapField(final SimpleName node, final IVariableBinding binding) {
        if (!binding.isField()) return;

        final ITypeBinding declaringClass = binding.getDeclaringClass();
        final MixinInformation mixinInfo = this.getMixinInfo(declaringClass);

        if (!mixinInfo.containsMixins) return;

        for (final IAnnotationBinding annotationBinding : binding.getAnnotations()) {
            // @Shadow
            if (Objects.equals("org.spongepowered.asm.mixin.Shadow", annotationBinding.getAnnotationType().getBinaryName())) {
                // Get mapping of target field
                final FieldMapping target = mixinInfo.target.computeFieldMapping(convertSignature(binding)).orElse(null);
                if (target == null) continue;

                // Create mapping for mixin
                final ClassMapping<?, ?> classMapping = this.mappings.getOrCreateClassMapping(declaringClass.getBinaryName());
                final FieldMapping field = classMapping.getOrCreateFieldMapping(convertSignature(binding));
                field.setDeobfuscatedName(target.getDeobfuscatedName());

                // todo: is there any fields in @Shadow we need to consider?
            }
        }
    }

    private void visit(SimpleName node, IBinding binding) {
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
    public final boolean visit(SimpleName node) {
        final IBinding binding = node.resolveBinding();
        if (binding != null) {
            visit(node, binding);
        }
        return false;
    }

    static class MixinInformation {

        final boolean containsMixins;
        final ClassMapping<?, ?> target;

        MixinInformation(final boolean containsMixins, final ClassMapping<?, ?> target) {
            this.containsMixins = containsMixins;
            this.target = target;
        }

    }

}
