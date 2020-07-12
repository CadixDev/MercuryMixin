/*
 * This file is part of Mixin, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.cadixdev.mercury.mixin.annotation;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Accessor Name struct.
 *
 * @author Adam Mummery-Smith
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public final class AccessorName {

    /**
     * Get an accessor name from the supplied string. If the string matches
     * the accessor name regex, split the string into the relevant parts
     *
     * @param methodName Name of the accessor method
     * @return Parsed AccessorName struct or null if the name is not a valid
     * accessor name
     */
    public static AccessorName of(final String methodName) {
        return AccessorName.of(methodName, true);
    }

    /**
     * Get an accessor name from the supplied string. If the string matches
     * the accessor name regex, split the string into the relevant parts
     *
     * @param methodName Name of the accessor method
     * @param toMemberCase True if the first character of the name should be
     * conditionally converted to lowercase. If the name is all
     * uppercase (eg. if the NAME_IS_A_CONSTANT) the first character
     * will not be lowercased, regardless of the state of this argument
     * @return Parsed AccessorName struct or null if the name is not a valid
     * accessor name
     */
    public static AccessorName of(final String methodName, final boolean toMemberCase) {
        final Matcher nameMatcher = AccessorName.PATTERN.matcher(methodName);
        if (nameMatcher.matches()) {
            final String prefix = nameMatcher.group(1);
            final String namePart = nameMatcher.group(2);
            final String firstChar = nameMatcher.group(3);
            final String remainder = nameMatcher.group(4);
            final boolean nameIsUpperCase = AccessorName.isUpperCase(Locale.ROOT, namePart);
            // If the entire name is upper case, do not lowercase the first char
            final String name = String.format("%s%s", AccessorName.toLowerCaseIf(Locale.ROOT, firstChar, toMemberCase && !nameIsUpperCase), remainder);
            return new AccessorName(methodName, prefix, name);
        }
        return null;
    }

    /**
     * Pattern for matching accessor names (for inflector)
     */
    private static final Pattern PATTERN = Pattern.compile("^(" + Arrays.stream(AccessorType.values())
            .map(AccessorType::getPrefixes)
            .flatMap(Arrays::stream)
            .collect(Collectors.joining("|")) + ")(([A-Z])(.*?))(_\\$md.*)?$");

    /**
     * Name of the accessor method
     */
    private final String methodName;

    /**
     * Accessor prefix
     */
    private final String prefix;

    /**
     * Accessor name part
     */
    private final String name;

    private AccessorName(final String methodName, final String prefix, final String name) {
        this.methodName = methodName;
        this.prefix = prefix;
        this.name = name;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getName() {
        return this.name;
    }

    public String prefix(final String name) {
        final String firstChar = name.substring(0, 1);
        final String remainder = name.substring(1);
        return this.prefix + toUpperCaseIf(Locale.ROOT, firstChar) + remainder;
    }

    private static boolean isUpperCase(final Locale locale, final String string) {
        return string.toUpperCase(locale).equals(string);
    }

    private static String toLowerCaseIf(final Locale locale, final String string, final boolean condition) {
        return condition ? string.toLowerCase(locale) : string;
    }

    private static String toUpperCaseIf(final Locale locale, final String string) {
        return string.toUpperCase(locale);
    }

}
