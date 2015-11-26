/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2015 ForgeRock AS.
 */

package org.forgerock.openam.sm.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation represents a console configuration attribute.
 *
 * @since 13.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface ConfigAttribute {

    /**
     * Gets value key representing the attribute key.
     *
     * @return attribute key
     */
    String value();

    /**
     * Whether the attribute is required; true by default.
     *
     * @return whether the attribute is required
     */
    boolean required() default true;

    /**
     * Provides a custom transformation class.
     *
     * @return custom transformation class
     */
    Class<? extends ConfigTransformer<?>> transformer() default DefaultConfigTransformer.class;

    /**
     * Default values if this attribute is not required and no config values are present.
     *
     * @return default values
     */
    String[] defaultValues() default {};

}
