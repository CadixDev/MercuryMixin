/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin;

import static org.cadixdev.mercury.mixin.util.MixinConstants.ACCESSOR_CLASS;
import static org.cadixdev.mercury.mixin.util.MixinConstants.INJECT_CLASS;
import static org.cadixdev.mercury.mixin.util.MixinConstants.OVERWRITE_CLASS;
import static org.cadixdev.mercury.mixin.util.MixinConstants.SHADOW_CLASS;
import static org.cadixdev.mercury.util.BombeBindings.convertType;

import org.cadixdev.bombe.analysis.InheritanceProvider;
import org.cadixdev.bombe.type.FieldType;
import org.cadixdev.bombe.type.MethodDescriptor;
import org.cadixdev.bombe.type.VoidType;
import org.cadixdev.bombe.type.signature.FieldSignature;
import org.cadixdev.bombe.type.signature.MethodSignature;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.FieldMapping;
import org.cadixdev.lorenz.model.Mapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.cadixdev.mercury.RewriteContext;
import org.cadixdev.mercury.analysis.MercuryInheritanceProvider;
import org.cadixdev.mercury.mixin.annotation.AccessorData;
import org.cadixdev.mercury.mixin.annotation.AccessorName;
import org.cadixdev.mercury.mixin.annotation.InjectData;
import org.cadixdev.mercury.mixin.annotation.MixinData;
import org.cadixdev.mercury.mixin.annotation.OverwriteData;
import org.cadixdev.mercury.mixin.annotation.ShadowData;
import org.cadixdev.mercury.util.BombeBindings;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

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

        for (final IAnnotationBinding annotation : binding.getAnnotations()) {
            final String annotationType = annotation.getAnnotationType().getBinaryName();

            // @Shadow
            if (Objects.equals(SHADOW_CLASS, annotationType)) {
                final ShadowData shadow = ShadowData.from(annotation);

                final boolean usedPrefix = binding.getName().startsWith(shadow.getPrefix());
                final FieldSignature targetSignature = convertSignature(shadow.stripPrefix(binding.getName()), binding.getType());
                final FieldSignature mixinSignature = BombeBindings.convertSignature(binding);

                // Get mapping of target field
                final FieldMapping targetField = target.computeFieldMapping(targetSignature).orElse(null);
                remapMixinField(
                        declaringClass,
                        mixinSignature,
                        targetField,
                        deobfName -> usedPrefix ? shadow.prefix(deobfName) : deobfName
                );
            }
        }
    }

    private void remapMethod(final SimpleName node, final IMethodBinding binding) {
        if (binding.isConstructor()) return;

        final ITypeBinding declaringClass = binding.getDeclaringClass();
        final MixinData mixin = MixinData.fetch(declaringClass);
        if (mixin == null) return;

        // todo: support multiple targets properly
        final ITypeBinding targetClass = mixin.getTargets()[0];
        final ClassMapping<?, ?> target = this.mappings.computeClassMapping(targetClass.getBinaryName()).orElse(null);
        if (target == null) return;
        target.complete(this.inheritanceProvider, declaringClass);

        for (final IAnnotationBinding annotation : binding.getAnnotations()) {
            final String annotationType = annotation.getAnnotationType().getBinaryName();

            // @Shadow
            if (Objects.equals(SHADOW_CLASS, annotationType)) {
                final ShadowData shadow = ShadowData.from(annotation);

                final boolean usedPrefix = binding.getName().startsWith(shadow.getPrefix());
                final MethodSignature targetSignature = convertSignature(shadow.stripPrefix(binding.getName()), binding);
                final MethodSignature mixinSignature = BombeBindings.convertSignature(binding);

                // Get mapping of target method
                final MethodMapping targetMethod = target.getMethodMapping(targetSignature).orElse(null);
                remapMixinMethod(
                        declaringClass,
                        mixinSignature,
                        targetMethod,
                        deobfName -> usedPrefix ? shadow.prefix(deobfName) : deobfName
                );
            }

            // @Overwrite
            if (Objects.equals(OVERWRITE_CLASS, annotationType)) {
                final OverwriteData overwrite = OverwriteData.from(annotation);

                final MethodSignature mixinSignature = BombeBindings.convertSignature(binding);

                // Get mapping of target method
                final MethodMapping targetMethod = target.getMethodMapping(mixinSignature).orElse(null);
                remapMixinMethod(
                        declaringClass,
                        mixinSignature,
                        targetMethod
                );
            }
        }
    }

    @Override
    public boolean visit(final MethodDeclaration node) {
        final AST ast = this.context.getCompilationUnit().getAST();
        final IMethodBinding binding = node.resolveBinding();

        final ITypeBinding declaringClass = binding.getDeclaringClass();
        final MixinData mixin = MixinData.fetch(declaringClass);
        if (mixin == null) return true;

        // todo: support multiple targets properly
        final ITypeBinding targetClass = mixin.getTargets()[0];
        final ClassMapping<?, ?> target = this.mappings.computeClassMapping(targetClass.getBinaryName()).orElse(null);
        if (target == null) return true;
        target.complete(this.inheritanceProvider, declaringClass);

        for (int i = 0; i < binding.getAnnotations().length; i++) {
            final IAnnotationBinding annotation = binding.getAnnotations()[i];
            final String annotationType = annotation.getAnnotationType().getBinaryName();

            // @Accessor
            if (Objects.equals(ACCESSOR_CLASS, annotationType)) {
                final AccessorName name = AccessorName.of(binding.getName());
                final AccessorData accessor = AccessorData.from(annotation);
                final MethodSignature mixinSignature = BombeBindings.convertSignature(binding);

                final boolean isGetter = mixinSignature.getDescriptor().getParamTypes().size() == 0 &&
                        !Objects.equals(VoidType.INSTANCE, mixinSignature.getDescriptor().getReturnType());
                final boolean isSetter = mixinSignature.getDescriptor().getParamTypes().size() == 1 &&
                        Objects.equals(VoidType.INSTANCE, mixinSignature.getDescriptor().getReturnType());

                // Inflect target from method name, if not set in annotation
                final boolean inflect = accessor.getTarget().isEmpty();
                final String targetName = inflect ? name.getName() : accessor.getTarget();
                final FieldSignature targetSignature = new FieldSignature(targetName, isGetter ?
                        // For getters, use the return type
                        (FieldType) mixinSignature.getDescriptor().getReturnType() :
                        // For setters, use the first argument in the method
                        mixinSignature.getDescriptor().getParamTypes().get(0)
                );

                // Get mapping of target field
                final FieldMapping targetField = target.computeFieldMapping(targetSignature).orElse(null);
                if (targetField == null) continue;

                // Inflect target name from name of method
                if (inflect) {
                    remapMixinMethod(
                            declaringClass,
                            mixinSignature,
                            targetField,
                            name::prefix
                    );
                }
                else {
                    final Annotation rawAnnotation = (Annotation) node.modifiers().get(i);

                    if (rawAnnotation.isNormalAnnotation()) {
                        final NormalAnnotation annotationNode = (NormalAnnotation) rawAnnotation;

                        for (final Object raw : annotationNode.values()) {
                            final MemberValuePair pair = (MemberValuePair) raw;

                            // Remap the method pair
                            if (Objects.equals("value", pair.getName().getIdentifier())) {
                                final StringLiteral original = (StringLiteral) pair.getValue();
                                replaceStringLiteral(ast, this.context, original, targetField.getDeobfuscatedName());
                            }
                        }
                    }
                    else if (rawAnnotation.isSingleMemberAnnotation()) {
                        final SingleMemberAnnotation annotationNode = (SingleMemberAnnotation) rawAnnotation;
                        final StringLiteral original = (StringLiteral) annotationNode.getValue();
                        replaceStringLiteral(ast, this.context, original, targetField.getDeobfuscatedName());
                    }
                    else {
                        throw new RuntimeException("Unexpected annotation: " + rawAnnotation.getClass().getName());
                    }
                }
            }

            // @Inject
            if (Objects.equals(INJECT_CLASS, annotationType)) {
                final InjectData inject = InjectData.from(annotation);

                // Find target method(s?)
                // todo: implement selectors
                final String[] method = new String[inject.getMethod().length];
                for (int j = 0; j < inject.getMethod().length; j++) {
                    final String targetMethod = inject.getMethod()[j];
                    String deobf = targetMethod;

                    for (final MethodMapping mapping : target.getMethodMappings()) {
                        if (Objects.equals(targetMethod, mapping.getObfuscatedName())) {
                            deobf = mapping.getDeobfuscatedName();
                            break;
                        }
                    }

                    method[j] = deobf;
                }

                final NormalAnnotation originalAnnotation = (NormalAnnotation) node.modifiers().get(i);

                for (final Object raw : originalAnnotation.values()) {
                    final MemberValuePair pair = (MemberValuePair) raw;

                    // Remap the method pair
                    if (Objects.equals("method", pair.getName().getIdentifier())) {
                        if (pair.getValue() instanceof StringLiteral) {
                            final StringLiteral original = (StringLiteral) pair.getValue();
                            replaceStringLiteral(ast, this.context, original, method[0]);
                        }
                        else {
                            final ArrayInitializer array = (ArrayInitializer) pair.getValue();
                            for (int j = 0; j < array.expressions().size(); j++) {
                                final StringLiteral original = (StringLiteral) array.expressions().get(j);
                                replaceStringLiteral(ast, this.context, original, method[j]);
                            }
                        }
                    }

                    // todo: handle @At remapping
                }
            }
        }

        return true;
    }

    private void visit(final SimpleName node, final IBinding binding) {
        switch (binding.getKind()) {
            case IBinding.VARIABLE:
                remapField(node, ((IVariableBinding) binding).getVariableDeclaration());
                break;
            case IBinding.METHOD:
                remapMethod(node, ((IMethodBinding) binding).getMethodDeclaration());
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

    private void remapMixinField(final ITypeBinding declaringClass,
                                 final FieldSignature mixinSignature,
                                 final Mapping<?, ?> target) {
        this.remapMixinField(declaringClass, mixinSignature, target, s -> s);
    }

    private void remapMixinField(final ITypeBinding declaringClass,
                                 final FieldSignature mixinSignature,
                                 final Mapping<?, ?> target,
                                 final Function<String, String> deobfProcessor) {
        if (target == null) return;

        // Create mapping for mixin
        final ClassMapping<?, ?> classMapping = this.mappings.getOrCreateClassMapping(declaringClass.getBinaryName());
        final FieldMapping field = classMapping.getOrCreateFieldMapping(mixinSignature);
        field.setDeobfuscatedName(deobfProcessor.apply(target.getDeobfuscatedName()));
    }

    private void remapMixinMethod(final ITypeBinding declaringClass,
                                 final MethodSignature mixinSignature,
                                 final Mapping<?, ?> target) {
        this.remapMixinMethod(declaringClass, mixinSignature, target, s -> s);
    }

    private void remapMixinMethod(final ITypeBinding declaringClass,
                                 final MethodSignature mixinSignature,
                                 final Mapping<?, ?> target,
                                 final Function<String, String> deobfProcessor) {
        if (target == null) return;

        // Create mapping for mixin
        final ClassMapping<?, ?> classMapping = this.mappings.getOrCreateClassMapping(declaringClass.getBinaryName());
        final MethodMapping method = classMapping.getOrCreateMethodMapping(mixinSignature);
        method.setDeobfuscatedName(deobfProcessor.apply(target.getDeobfuscatedName()));
    }

    private static void replaceStringLiteral(final AST ast, final RewriteContext context, final StringLiteral original, final String replacement) {
        final StringLiteral replacementLiteral = ast.newStringLiteral();
        replacementLiteral.setLiteralValue(replacement);
        context.createASTRewrite().replace(original, replacementLiteral, null);
    }

    private static FieldSignature convertSignature(final String name, final ITypeBinding type) {
        return new FieldSignature(name, (FieldType) convertType(type));
    }

    private static MethodSignature convertSignature(final String name, final IMethodBinding binding) {
        final ITypeBinding[] parameterBindings = binding.getParameterTypes();
        final List<FieldType> parameters = new ArrayList<>(parameterBindings.length);

        for (final ITypeBinding parameterBinding : parameterBindings) {
            parameters.add((FieldType) convertType(parameterBinding));
        }

        return new MethodSignature(name, new MethodDescriptor(parameters, convertType(binding.getReturnType())));
    }

}
