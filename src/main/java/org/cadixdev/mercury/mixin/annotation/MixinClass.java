/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.annotation;

import org.cadixdev.bombe.type.signature.FieldSignature;
import org.cadixdev.bombe.type.signature.MethodSignature;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.FieldMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.function.Function;

/**
 * Represents a single mixin, acting as a holder for the annotations on
 * the class.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public class MixinClass extends MixinData {

    public static MixinClass fetch(final ITypeBinding declaringClass, final MappingSet mappings) {
        final MixinData mixinData = MixinData.fetch(declaringClass);
        if (mixinData == null) return null;

        return new MixinClass(
                declaringClass, mappings,
                mixinData.getTargets(), mixinData.getPrivateTargets()
        );
    }

    private final ITypeBinding binding;
    private final MappingSet mappings;

    private ClassMapping<?, ?> mixinMapping;

    public MixinClass(
            // General
            final ITypeBinding binding, final MappingSet mappings,
            // @Mixin(value = { *.class }, targets = { "*" })
            final ITypeBinding[] targets, final String[] privateTargets
    ) {
        super(targets, privateTargets);
        this.binding = binding;
        this.mappings = mappings;
    }

    /**
     * Gets the binding for the mixin class.
     *
     * @return The mixin binding
     */
    public ITypeBinding getBinding() {
        return this.binding;
    }

    /**
     * Gets the <em>binary names</em> of all the mixin's targets, both
     * public and private.
     *
     * @return The targets
     */
    public String[] getTargetNames() {
        final String[] targets = new String[this.targets.length + this.privateTargets.length];
        for (int i = 0; i < this.targets.length; i++) {
            targets[i] = this.targets[i].getBinaryName();
        }
        System.arraycopy(this.privateTargets, 0, targets, this.targets.length, this.privateTargets.length);
        return targets;
    }

    /**
     * Gets the {@link ClassMapping mapping} for this mixin class, creating the
     * mapping on-demand.
     *
     * @return The mapping
     */
    public ClassMapping<?, ?> getMixinMapping() {
        if (this.mixinMapping != null) return this.mixinMapping;
        return this.mixinMapping = this.mappings.getOrCreateClassMapping(this.binding.getBinaryName());
    }

    /**
     * Copies the relevant {@link FieldMapping mapping} from the target's {@link ClassMapping mappings},
     * if such information exists.
     * <p>
     * To facilitate prefixing(/suffixing/etc), you can use a "deobf processor" argument to process the
     * de-obfuscation name applied to the mixin's mapping.
     *
     * @param targetParent The {@link ClassMapping class mapping} of the mixin's target you wish to copy
     *                     the given field mapping from
     * @param mixinSignature The {@link FieldSignature signature} of the field in the mixin
     * @param targetSignature The {@link FieldSignature signature} of the field in the target
     * @param deobfProcessor A processor to alter the de-obfuscation name before it is applied to the
     *                       copied mapping
     */
    public void copyFieldMapping(final ClassMapping<?, ?> targetParent,
                                 final FieldSignature mixinSignature, final FieldSignature targetSignature,
                                 final Function<String, String> deobfProcessor) {
        final FieldMapping targetField = targetParent.computeFieldMapping(targetSignature).orElse(null);
        if (targetField == null) return;
        final FieldMapping mixinField = this.getMixinMapping().computeFieldMapping(mixinSignature)
                .orElseGet(() -> this.getMixinMapping().createFieldMapping(mixinSignature));

        // Copy de-obf information
        mixinField.setDeobfuscatedName(deobfProcessor.apply(targetField.getDeobfuscatedName()));
    }

    /**
     * Copies the relevant {@link FieldMapping mapping} from the target's {@link ClassMapping mappings},
     * if such information exists.
     * <p>
     * To facilitate prefixing(/suffixing/etc), you can use a "deobf processor" argument to process the
     * de-obfuscation name applied to the mixin's mapping.
     *
     * @param targetParent The {@link ClassMapping class mapping} of the mixin's target you wish to copy
     *                     the given field mapping from
     * @param signature The {@link FieldSignature signature} of the field you wish to copy
     * @param deobfProcessor A processor to alter the de-obfuscation name before it is applied to the
     *                       copied mapping
     */
    public void copyFieldMapping(final ClassMapping<?, ?> targetParent, final FieldSignature signature,
                                 final Function<String, String> deobfProcessor) {
        this.copyFieldMapping(targetParent, signature, signature, deobfProcessor);
    }

    /**
     * Copies the relevant {@link MethodMapping mapping} from the target's {@link ClassMapping mappings},
     * if such information exists.
     * <p>
     * To facilitate prefixing(/suffixing/etc), you can use a "deobf processor" argument to process the
     * de-obfuscation name applied to the mixin's mapping.
     *
     * @param targetParent The {@link ClassMapping class mapping} of the mixin's target you wish to copy
     *                     the given method mapping from
     * @param mixinSignature The {@link MethodSignature signature} of the field in the mixin
     * @param targetSignature The {@link MethodSignature signature} of the field in the target
     * @param deobfProcessor A processor to alter the de-obfuscation name before it is applied to the
     *                       copied mapping
     */
    public void copyMethodMapping(final ClassMapping<?, ?> targetParent,
                                  final MethodSignature mixinSignature, final MethodSignature targetSignature,
                                  final Function<String, String> deobfProcessor) {
        final MethodMapping targetMethod = targetParent.getMethodMapping(targetSignature).orElse(null);
        if (targetMethod == null) return;
        final MethodMapping mixinMethod = this.getMixinMapping().getOrCreateMethodMapping(mixinSignature);

        // Copy de-obf information
        mixinMethod.setDeobfuscatedName(deobfProcessor.apply(targetMethod.getDeobfuscatedName()));
    }

    /**
     * Copies the relevant {@link MethodMapping mapping} from the target's {@link ClassMapping mappings},
     * if such information exists.
     * <p>
     * To facilitate prefixing(/suffixing/etc), you can use a "deobf processor" argument to process the
     * de-obfuscation name applied to the mixin's mapping.
     *
     * @param targetParent The {@link ClassMapping class mapping} of the mixin's target you wish to copy
     *                     the given method mapping from
     * @param signature The {@link MethodSignature signature} of the method you wish to copy
     * @param deobfProcessor A processor to alter the de-obfuscation name before it is applied to the
     *                       copied mapping
     */
    public void copyMethodMapping(final ClassMapping<?, ?> targetParent, final MethodSignature signature,
                                  final Function<String, String> deobfProcessor) {
        this.copyMethodMapping(targetParent, signature, signature, deobfProcessor);
    }

    /**
     * Copies the relevant {@link FieldMapping mapping} from the target's {@link ClassMapping mappings},
     * if such information exists.
     * <p>
     * To facilitate prefixing(/suffixing/etc), you can use a "deobf processor" argument to process the
     * de-obfuscation name applied to the mixin's mapping.
     *
     * @param targetParent The {@link ClassMapping class mapping} of the mixin's target you wish to copy
     *                     the given mapping from
     * @param mixinSignature The {@link MethodSignature signature} of the field in the mixin
     * @param targetSignature The {@link FieldSignature signature} of the field in the target
     * @param deobfProcessor A processor to alter the de-obfuscation name before it is applied to the
     *                       copied mapping
     */
    public void copyMethodMapping(final ClassMapping<?, ?> targetParent,
                                  final MethodSignature mixinSignature, final FieldSignature targetSignature,
                                  final Function<String, String> deobfProcessor) {
        final FieldMapping targetField = targetParent.computeFieldMapping(targetSignature).orElse(null);
        if (targetField == null) return;
        final MethodMapping mixinMethod = this.getMixinMapping().getOrCreateMethodMapping(mixinSignature);

        // Copy de-obf information
        mixinMethod.setDeobfuscatedName(deobfProcessor.apply(targetField.getDeobfuscatedName()));
    }

}
