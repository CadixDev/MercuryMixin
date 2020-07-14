/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin;

import static org.cadixdev.mercury.mixin.annotation.AccessorType.FIELD_GETTER;
import static org.cadixdev.mercury.mixin.util.MixinConstants.ACCESSOR_CLASS;
import static org.cadixdev.mercury.mixin.util.MixinConstants.INJECT_CLASS;
import static org.cadixdev.mercury.mixin.util.MixinConstants.INVOKER_CLASS;
import static org.cadixdev.mercury.mixin.util.MixinConstants.MODIFY_CONSTANT_CLASS;
import static org.cadixdev.mercury.mixin.util.MixinConstants.MODIFY_VARIABLE_CLASS;
import static org.cadixdev.mercury.mixin.util.MixinConstants.OVERWRITE_CLASS;
import static org.cadixdev.mercury.mixin.util.MixinConstants.REDIRECT_CLASS;
import static org.cadixdev.mercury.mixin.util.MixinConstants.SHADOW_CLASS;
import static org.cadixdev.mercury.util.BombeBindings.convertType;

import org.cadixdev.bombe.analysis.InheritanceProvider;
import org.cadixdev.bombe.type.FieldType;
import org.cadixdev.bombe.type.MethodDescriptor;
import org.cadixdev.bombe.type.Type;
import org.cadixdev.bombe.type.signature.FieldSignature;
import org.cadixdev.bombe.type.signature.MethodSignature;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.FieldMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.cadixdev.mercury.RewriteContext;
import org.cadixdev.mercury.analysis.MercuryInheritanceProvider;
import org.cadixdev.mercury.mixin.annotation.AccessorData;
import org.cadixdev.mercury.mixin.annotation.AccessorName;
import org.cadixdev.mercury.mixin.annotation.AccessorType;
import org.cadixdev.mercury.mixin.annotation.AtData;
import org.cadixdev.mercury.mixin.annotation.InjectData;
import org.cadixdev.mercury.mixin.annotation.InjectTarget;
import org.cadixdev.mercury.mixin.annotation.MixinClass;
import org.cadixdev.mercury.mixin.annotation.ShadowData;
import org.cadixdev.mercury.util.BombeBindings;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MixinRemapperVisitor extends ASTVisitor {

    final RewriteContext context;
    final MappingSet mappings;
    private final InheritanceProvider inheritanceProvider;

    MixinRemapperVisitor(final RewriteContext context, final MappingSet mappings) {
        this.context = context;
        this.mappings = mappings;
        this.inheritanceProvider = MercuryInheritanceProvider.get(context.getMercury());
    }

    private void remapPrivateMixinTarget(final AST ast, final TypeDeclaration typeDeclaration, final ITypeBinding binding) {
        for (final Object rawModifier : typeDeclaration.modifiers()) {
            final IExtendedModifier modifier = (IExtendedModifier) rawModifier;
            if (!modifier.isAnnotation()) return;
            final Annotation rawAnnot = (Annotation) modifier;

            if (rawAnnot.isNormalAnnotation()) {
                final NormalAnnotation annot = (NormalAnnotation) rawAnnot;

                for (final Object raw : annot.values()) {
                    final MemberValuePair pair = (MemberValuePair) raw;

                    if (Objects.equals("targets", pair.getName().getIdentifier())) {
                        final Expression targets = pair.getValue();

                        if (targets instanceof StringLiteral) {
                            final StringLiteral target = (StringLiteral) targets;
                            this.remapPrivateMixinTargetLiteral(ast, target);
                        }
                        else if (targets instanceof ArrayInitializer) {
                            final ArrayInitializer target = (ArrayInitializer) targets;

                            for (final Object expression : target.expressions()) {
                                if (expression instanceof StringLiteral) {
                                    this.remapPrivateMixinTargetLiteral(ast, (StringLiteral) expression);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void remapPrivateMixinTargetLiteral(final AST ast, final StringLiteral literal) {
        final String className = literal.getLiteralValue();
        if (className.isEmpty()) return;
        final boolean binaryFormat = className.contains("/");

        ClassMapping<?, ?> classMapping = this.mappings.getTopLevelClassMapping(className).orElse(null);
        if (classMapping == null) {
            classMapping = this.mappings.getClassMapping(className).orElse(null);
        }

        if (classMapping != null) {
            final String remappedClassName = classMapping.getFullDeobfuscatedName();
            replaceExpression(ast, this.context, literal, binaryFormat ?
                    remappedClassName :
                    remappedClassName.replace('/', '.'));
        }
    }

    void remapField(final SimpleName node, final IVariableBinding binding) {
        if (!binding.isField()) return;

        final ITypeBinding declaringClass = binding.getDeclaringClass();
        if (declaringClass == null) return;

        final MixinClass mixin = MixinClass.fetch(declaringClass, this.mappings);
        if (mixin == null) return;

        // todo: support multiple targets properly
        final ClassMapping<?, ?> target = this.mappings.computeClassMapping(mixin.getTargetNames()[0]).orElse(null);
        if (target == null) return;

        for (final IAnnotationBinding annotation : binding.getAnnotations()) {
            final String annotationType = annotation.getAnnotationType().getBinaryName();

            // @Shadow
            if (Objects.equals(SHADOW_CLASS, annotationType)) {
                final ShadowData shadow = ShadowData.from(annotation);

                final boolean usedPrefix = binding.getName().startsWith(shadow.getPrefix());
                final FieldSignature targetSignature = convertSignature(shadow.stripPrefix(binding.getName()), binding.getType());
                final FieldSignature mixinSignature = BombeBindings.convertSignature(binding);

                // Copy de-obfuscation mapping
                mixin.copyFieldMapping(
                        target,
                        mixinSignature,
                        targetSignature,
                        deobfName -> usedPrefix ? shadow.prefix(deobfName) : deobfName
                );
            }
        }
    }

    @Override
    public boolean visit(final MethodDeclaration node) {
        final AST ast = this.context.getCompilationUnit().getAST();
        final IMethodBinding binding = node.resolveBinding();

        final ITypeBinding declaringClass = binding.getDeclaringClass();
        final MixinClass mixin = MixinClass.fetch(declaringClass, this.mappings);
        if (mixin == null) return true;

        // todo: support multiple targets properly
        final ClassMapping<?, ?> target = this.mappings.computeClassMapping(mixin.getTargetNames()[0]).orElse(null);
        if (target == null) return true;

        // todo: handle private targets
        // todo: only complete the mixin we are targeting
        for (final ITypeBinding mixinTarget : mixin.getTargets()) {
            target.complete(this.inheritanceProvider, mixinTarget);
        }

        for (int i = 0; i < binding.getAnnotations().length; i++) {
            final IAnnotationBinding annotation = binding.getAnnotations()[i];
            final String annotationType = annotation.getAnnotationType().getBinaryName();

            // @Shadow
            if (Objects.equals(SHADOW_CLASS, annotationType)) {
                final ShadowData shadow = ShadowData.from(annotation);

                final boolean usedPrefix = binding.getName().startsWith(shadow.getPrefix());
                final MethodSignature targetSignature = convertSignature(shadow.stripPrefix(binding.getName()), binding);
                final MethodSignature mixinSignature = BombeBindings.convertSignature(binding);

                // Copy de-obfuscation mapping
                mixin.copyMethodMapping(
                        target,
                        mixinSignature,
                        targetSignature,
                        deobfName -> usedPrefix ? shadow.prefix(deobfName) : deobfName
                );
            }

            // @Overwrite
            if (Objects.equals(OVERWRITE_CLASS, annotationType)) {
                final MethodSignature signature = BombeBindings.convertSignature(binding);

                // Copy de-obfuscation mapping
                mixin.copyMethodMapping(target, signature, s -> s);
            }

            // @Accessor and @Invoker
            if (Objects.equals(ACCESSOR_CLASS, annotationType) || Objects.equals(INVOKER_CLASS, annotationType)) {
                final AccessorName name = AccessorName.of(binding.getName());
                final AccessorData accessor = AccessorData.from(annotation);
                final MethodSignature mixinSignature = BombeBindings.convertSignature(binding);
                final AccessorType type = AccessorType.get(
                        Objects.equals(INVOKER_CLASS, annotationType),
                        binding, mixinSignature, accessor
                );

                // Inflect target from target name, if not set in annotation
                final boolean inflect = accessor.getTarget().isEmpty();
                final String targetName = inflect ? name.getName() : accessor.getTarget();

                switch (type) {
                    // @Accessor
                    case FIELD_GETTER:
                    case FIELD_SETTER: {
                        final FieldSignature targetSignature = new FieldSignature(targetName, type == FIELD_GETTER ?
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
                            mixin.copyMethodMapping(target, mixinSignature, targetSignature, name::prefix);
                        }
                        else {
                            final Annotation rawAnnotation = (Annotation) node.modifiers().get(i);
                            replaceValueInAnnotation(ast, this.context, rawAnnotation, targetField.getDeobfuscatedName());
                        }
                        break;
                    }

                    // @Invoker
                    case METHOD_PROXY: {
                        final MethodSignature targetSignature = new MethodSignature(targetName, mixinSignature.getDescriptor());

                        // Get mapping of target field
                        final MethodMapping targetMethod = target.getMethodMapping(targetSignature).orElse(null);
                        if (targetMethod == null) continue;

                        // Inflect target name from name of method
                        if (inflect) {
                            mixin.copyMethodMapping(target, mixinSignature, targetSignature, name::prefix);
                        }
                        else {
                            final Annotation rawAnnotation = (Annotation) node.modifiers().get(i);
                            replaceValueInAnnotation(ast, this.context, rawAnnotation, targetMethod.getDeobfuscatedName());
                        }
                        break;
                    }
                    case OBJECT_FACTORY: {
                        // @Invoker.value will always be either <init> or the target class name
                        if (!Objects.equals("<init>", accessor.getTarget())) {
                            // Remap target class name
                            final ClassMapping<?, ?> targetClass = this.mappings.computeClassMapping(accessor.getTarget()).orElse(null);
                            if (targetClass == null) continue;

                            final Annotation rawAnnotation = (Annotation) node.modifiers().get(i);
                            replaceValueInAnnotation(ast, this.context, rawAnnotation, targetClass.getFullDeobfuscatedName());
                        }
                        break;
                    }
                }
            }

            // @Inject, @Redirect, @ModifyConstant, & @ModifyVariable
            if (Objects.equals(INJECT_CLASS, annotationType)
                    || Objects.equals(REDIRECT_CLASS, annotationType)
                    || Objects.equals(MODIFY_CONSTANT_CLASS, annotationType)
                    || Objects.equals(MODIFY_VARIABLE_CLASS, annotationType)) {
                final InjectData inject = InjectData.from(annotation);

                // Find target method(s?)
                // todo: implement selectors
                final String[] injectTargets = new String[inject.getInjectTargets().length];
                for (int j = 0; j < inject.getInjectTargets().length; j++) {
                    final InjectTarget injectTarget = inject.getInjectTargets()[j];
                    injectTargets[j] = remapInjectTarget(target, injectTarget)
                            .orElse(injectTarget.getFullTarget());
                }

                final NormalAnnotation originalAnnotation = (NormalAnnotation) node.modifiers().get(i);
                int atIndex = 0;
                for (final Object raw : originalAnnotation.values()) {
                    final MemberValuePair pair = (MemberValuePair) raw;

                    // Remap the method pair
                    if (Objects.equals("method", pair.getName().getIdentifier())) {
                        if (pair.getValue() instanceof StringLiteral || pair.getValue() instanceof InfixExpression) {
                            replaceExpression(ast, this.context, pair.getValue(), injectTargets[0]);
                        }
                        else {
                            final ArrayInitializer array = (ArrayInitializer) pair.getValue();
                            for (int j = 0; j < array.expressions().size(); j++) {
                                final StringLiteral original = (StringLiteral) array.expressions().get(j);
                                replaceExpression(ast, this.context, original, injectTargets[j]);
                            }
                        }
                    }

                    // Remap @At
                    if (Objects.equals("at", pair.getName().getIdentifier())) {
                        // it could be a SingleMemberAnnotation here but we don't care about that case

                        if (pair.getValue() instanceof ArrayInitializer) {
                            final ArrayInitializer value = (ArrayInitializer) pair.getValue();

                            for (final Object expression : value.expressions()) {
                                if (expression instanceof NormalAnnotation) {
                                    final NormalAnnotation atAnnotation = (NormalAnnotation) expression;

                                    final AtData atDatum = inject.getAtData()[atIndex];
                                    remapAtAnnotation(ast, declaringClass, atAnnotation, atDatum);
                                }
                                atIndex++;
                            }
                        }
                        else if (pair.getValue() instanceof NormalAnnotation) {
                            final NormalAnnotation atAnnotation = (NormalAnnotation) pair.getValue();

                            final AtData atDatum = inject.getAtData()[atIndex];
                            remapAtAnnotation(ast, declaringClass, atAnnotation, atDatum);
                        }
                    }
                }
            }
        }

        return true;
    }

    private Optional<String> remapInjectTarget(final ClassMapping<?, ?> target, final InjectTarget injectTarget) {
        final String targetName = injectTarget.getTargetName();

        if (injectTarget.getFieldType().isPresent()) {
            // this is targeting a field
            final Type fieldType = injectTarget.getFieldType().get();

            for (final FieldMapping mapping : target.getFieldMappings()) {
                if (Objects.equals(targetName, mapping.getObfuscatedName())) {
                    if (mapping.getType().isPresent() && !Objects.equals(mapping.getType().get(), fieldType)) {
                        // the mapping has a type but it is different than the target type
                        continue;
                    }

                    final FieldSignature deobfuscatedSignature = mapping.getDeobfuscatedSignature();
                    final String deobfuscatedFieldType = deobfuscatedSignature.getType()
                            .map(FieldType::toString)
                            .orElse(null);

                    return Optional.of(deobfuscatedFieldType != null ?
                            deobfuscatedSignature.getName() + ":" + deobfuscatedFieldType :
                            deobfuscatedSignature.getName());
                }
            }
        }
        else {
            // this is probably targeting a method
            for (final MethodMapping mapping : target.getMethodMappings()) {
                if (Objects.equals(targetName, mapping.getObfuscatedName()) &&
                        injectTarget.getMethodDescriptor()
                                .map(d -> d.equals(mapping.getDescriptor()))
                                .orElse(true)) {
                    final MethodSignature deobfuscatedSignature = mapping.getDeobfuscatedSignature();

                    return Optional.of(injectTarget.getMethodDescriptor().isPresent() ?
                            deobfuscatedSignature.getName() + deobfuscatedSignature.getDescriptor().toString() :
                            deobfuscatedSignature.getName());
                }
            }
        }
        return Optional.empty();
    }

    private void remapAtAnnotation(final AST ast, final ITypeBinding declaringClass, final NormalAnnotation atAnnotation, final AtData atDatum) {
        for (final Object atRaw : atAnnotation.values()) {
            // this will always be a MemberValuePair
            final MemberValuePair atRawPair = (MemberValuePair) atRaw;

            // check for the target
            if (Objects.equals("target", atRawPair.getName().getIdentifier())) {
                // make sure everything is present
                if (atDatum.getClassName().isPresent()) {
                    final String className = atDatum.getClassName().get();
                    final Expression originalTarget = atRawPair.getValue();

                    // get the class mapping of the class that owns the target we're remapping
                    final ClassMapping<?, ?> atTargetMappings = this.mappings.computeClassMapping(className).orElse(null);
                    if (atTargetMappings == null) continue;

                    final String deobfTargetClass = atTargetMappings.getFullDeobfuscatedName();

                    if (atDatum.getTarget().isPresent()) {
                        remapInjectTarget(atTargetMappings, atDatum.getTarget().get());
                        // class name + method signature
                        final InjectTarget atTarget = atDatum.getTarget().get();
                        final Optional<MethodDescriptor> methodDescriptor = atTarget.getMethodDescriptor();
                        final Optional<Type> fieldType = atTarget.getFieldType();
                        // the method descriptor should always be present in an @At's target
                        if (methodDescriptor.isPresent()) {
                            final MethodMapping methodMapping = atTargetMappings.getMethodMapping(atTarget.getTargetName(), methodDescriptor.get().toString()).orElse(null);
                            if (methodMapping == null) continue;

                            // replace the original literal with class + method + method sig
                            final MethodSignature deobfuscatedSignature = methodMapping.getDeobfuscatedSignature();
                            final String deobfTargetSig = deobfuscatedSignature.getName() + deobfuscatedSignature.getDescriptor().toString();
                            final String deobfTarget = "L" + deobfTargetClass + ";" + deobfTargetSig;
                            replaceExpression(ast, this.context, originalTarget, deobfTarget);
                        }
                        else if (fieldType.isPresent()) {
                            final FieldMapping fieldMapping = atTargetMappings.computeFieldMapping(FieldSignature.of(atTarget.getTargetName(), fieldType.get().toString())).orElse(null);
                            if (fieldMapping == null) continue;

                            final String deobfTargetSig = fieldMapping.getDeobfuscatedName() + ":" + fieldType.get();
                            final String deobfTarget = "L" + deobfTargetClass + ";" + deobfTargetSig;
                            replaceExpression(ast, this.context, originalTarget, deobfTarget);
                        }
                    }
                    else {
                        // it's just the class name
                        replaceExpression(ast, this.context, originalTarget, deobfTargetClass);
                    }
                }
            }
        }
    }

    private void visit(final SimpleName node, final IBinding binding) {
        switch (binding.getKind()) {
            case IBinding.VARIABLE:
                this.remapField(node, ((IVariableBinding) binding).getVariableDeclaration());
                break;
        }
    }

    @Override
    public final boolean visit(final SimpleName node) {
        final IBinding binding = node.resolveBinding();
        if (binding != null) {
            this.visit(node, binding);
        }
        return false;
    }

    @Override
    public boolean visit(final TypeDeclaration node) {
        this.remapPrivateMixinTarget(node.getAST(), node, node.resolveBinding());
        return true;
    }

    private static void replaceExpression(final AST ast, final RewriteContext context, final Expression original, final String replacement) {
        final StringLiteral replacementLiteral = ast.newStringLiteral();
        replacementLiteral.setLiteralValue(replacement);
        context.createASTRewrite().replace(original, replacementLiteral, null);
    }

    private static void replaceValueInAnnotation(final AST ast, final RewriteContext context, final Annotation rawAnnotation, final String replacement) {
        if (rawAnnotation.isNormalAnnotation()) {
            final NormalAnnotation annotationNode = (NormalAnnotation) rawAnnotation;

            for (final Object raw : annotationNode.values()) {
                final MemberValuePair pair = (MemberValuePair) raw;

                // Remap the method pair
                if (Objects.equals("value", pair.getName().getIdentifier())) {
                    final StringLiteral original = (StringLiteral) pair.getValue();
                    replaceExpression(ast, context, original, replacement);
                }
            }
        }
        else if (rawAnnotation.isSingleMemberAnnotation()) {
            final SingleMemberAnnotation annotationNode = (SingleMemberAnnotation) rawAnnotation;
            final StringLiteral original = (StringLiteral) annotationNode.getValue();
            replaceExpression(ast, context, original, replacement);
        }
        else {
            throw new RuntimeException("Unexpected annotation: " + rawAnnotation.getClass().getName());
        }
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
