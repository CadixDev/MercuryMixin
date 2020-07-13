/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.cleaner;

import static org.cadixdev.mercury.mixin.util.MixinConstants.FINAL_CLASS;
import static org.cadixdev.mercury.mixin.util.MixinConstants.MUTABLE_CLASS;
import static org.cadixdev.mercury.mixin.util.MixinConstants.SHADOW_CLASS;

import org.cadixdev.bombe.analysis.InheritanceProvider;
import org.cadixdev.bombe.type.signature.FieldSignature;
import org.cadixdev.mercury.RewriteContext;
import org.cadixdev.mercury.analysis.MercuryInheritanceProvider;
import org.cadixdev.mercury.mixin.annotation.MixinData;
import org.cadixdev.mercury.util.BombeBindings;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.Objects;

public class FinalVisitor extends ASTVisitor {

    final RewriteContext context;
    private final InheritanceProvider inheritanceProvider;

    FinalVisitor(final RewriteContext context) {
        this.context = context;
        this.inheritanceProvider = MercuryInheritanceProvider.get(context.getMercury());
    }

    @Override
    public boolean visit(final FieldDeclaration node) {
        for (final Object obj : node.fragments()) {
            final VariableDeclarationFragment fragment = (VariableDeclarationFragment) obj;
            final IVariableBinding binding = fragment.resolveBinding();
            final FieldSignature signature = BombeBindings.convertSignature(binding);

            final ITypeBinding declaringClass = binding.getDeclaringClass();
            final MixinData mixin = MixinData.fetch(declaringClass);
            if (mixin == null || mixin.getTargets().length == 0) continue;
            final ITypeBinding targetClass = mixin.getTargets()[0];

            int shadowIndex = -1;
            int mutableIndex = -1;
            int finalIndex = -1;

            for (int i = 0; i < binding.getAnnotations().length; i++) {
                final IAnnotationBinding annotation = binding.getAnnotations()[i];
                final String annotationType = annotation.getAnnotationType().getBinaryName();

                // @Shadow
                if (Objects.equals(SHADOW_CLASS, annotationType)) {
                    shadowIndex = i;
                }

                // @Mutable
                if (Objects.equals(MUTABLE_CLASS, annotationType)) {
                    mutableIndex = i;
                }

                // @Final
                if (Objects.equals(FINAL_CLASS, annotationType)) {
                    finalIndex = i;
                }
            }

            if (shadowIndex != -1) {
                boolean isTargetFinal = false;

                for (final IVariableBinding field : targetClass.getDeclaredFields()) {
                    final FieldSignature fieldSignature = BombeBindings.convertSignature(field);

                    // Check the target field is final or not
                    if (signature.equals(fieldSignature)) {
                        isTargetFinal = Modifier.isFinal(field.getModifiers());
                        break;
                    }
                }

                if (isTargetFinal) {
                    // Add @Final when target is final and @Mutable isn't present
                    if (finalIndex == -1 && mutableIndex == -1) {
                        final AST ast = this.context.getCompilationUnit().getAST();

                        final MarkerAnnotation finalA = ast.newMarkerAnnotation();
                        finalA.setTypeName(ast.newSimpleName("Final"));

                        this.context.createImportRewrite().addImport(FINAL_CLASS);
                        this.context.createASTRewrite().getListRewrite(node, FieldDeclaration.MODIFIERS2_PROPERTY).insertAt(finalA, shadowIndex + 1, null);
                    }
                }
                else {
                    // Remove @Mutable when target field isn't final
                    if (mutableIndex != -1) {
                        final ListRewrite rewrite = this.context.createASTRewrite().getListRewrite(node, FieldDeclaration.MODIFIERS2_PROPERTY);
                        final ASTNode mutableNode = (ASTNode) rewrite.getOriginalList().get(mutableIndex);
                        rewrite.remove(mutableNode, null);
                    }

                    // Remove @Final when target field isn't final
                    if (finalIndex != -1) {
                        final ListRewrite rewrite = this.context.createASTRewrite().getListRewrite(node, FieldDeclaration.MODIFIERS2_PROPERTY);
                        final ASTNode finalNode = (ASTNode) rewrite.getOriginalList().get(finalIndex);
                        rewrite.remove(finalNode, null);
                    }
                }
            }
        }

        return super.visit(node);
    }

}
