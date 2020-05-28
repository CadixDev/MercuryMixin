package org.cadixdev.mercury.mixin.annotation;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;

import java.util.Objects;
import java.util.Optional;

/**
 * A container for data held in the {@code @At} annotation.
 *
 * @author Jadon Fowler
 */
public class AtData {

    // @At(value = "", target = "")
    public static AtData from(final IAnnotationBinding binding) {
        String injectionPoint = null;
        String className = null;
        MethodTarget target = null;

        for (final IMemberValuePairBinding pair : binding.getDeclaredMemberValuePairs()) {
            if (Objects.equals("value", pair.getName())) {
                injectionPoint = (String) pair.getValue();
            } else if (Objects.equals("target", pair.getName())) {
                String combined = (String) pair.getValue();
                int semiIndex = combined.indexOf(';');
                className = combined.substring(1, semiIndex);
                target = MethodTarget.of(combined.substring(semiIndex + 1));
            }
        }

        return new AtData(injectionPoint, className, target);
    }

    private final String injectionPoint;
    private final String className;
    private final MethodTarget target;

    public AtData(final String injectionPoint, final String className, final MethodTarget target) {
        this.injectionPoint = injectionPoint;
        this.className = className;
        this.target = target;
    }

    public String getInjectionPoint() {
        return injectionPoint;
    }

    public Optional<String> getClassName() {
        return Optional.ofNullable(className);
    }

    public Optional<MethodTarget> getTarget() {
        return Optional.ofNullable(target);
    }

    @Override
    public String toString() {
        return "AtData{" +
                "injectionPoint='" + injectionPoint + '\'' +
                ", className='" + className + '\'' +
                ", target=" + target +
                '}';
    }

}
