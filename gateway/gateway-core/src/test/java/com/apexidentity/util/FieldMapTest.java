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
 * information: "Portions Copyrighted [year] [name of copyright owner]".
 *
 * Copyright © 2010–2011 ApexIdentity Inc. All rights reserved.
 */

package com.apexidentity.util;

import java.util.HashMap;
import java.util.HashSet;

import static org.fest.assertions.Assertions.assertThat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 * @author Paul C. Bryan
 */
public class FieldMapTest {

    public static class Fields {
        public static String staticField;
        public int primitiveField;
        public String stringField;
        public Boolean booleanField;
        private String privateField;
    }

    private Fields fields = new Fields();
    private FieldMap map = new FieldMap(fields);

    // ----- context -----------------------------------------------------------

    @BeforeMethod
    public void before() {
        fields.primitiveField = 1;
        fields.stringField = "FOOBAR";
        fields.booleanField = true;
        fields.privateField = "PRIVACY!";
    }

    // ----- field members -----------------------------------------------------

    @Test
    public void containsField() {
        assertThat(map.containsKey("stringField")).isTrue();
    }

    @Test
    public void notContainsStaticField() {
        assertThat(map.containsKey("staticField")).isFalse();
    }

    @Test
    public void notContainsPrimitiveField() {
        assertThat(map.containsKey("primitiveField")).isFalse();
    }

    @Test
    public void notContainsPrivateField() {
        assertThat(map.containsKey("privateField")).isFalse();
    }

    @Test
    public void notContainsNonExistentField() {
        assertThat(map.containsKey("notField")).isFalse();
    }

    @Test
    public void booleanType() {
        assertThat(map.get("booleanField")).isInstanceOf(Boolean.class);
    }

    @Test
    public void booleanFieldValue() {
        assertThat(map.get("booleanField")).isEqualTo(fields.booleanField);
    }

    @Test
    public void stringFieldValue() {
        assertThat(map.get("stringField")).isEqualTo(fields.stringField);
    }
}
