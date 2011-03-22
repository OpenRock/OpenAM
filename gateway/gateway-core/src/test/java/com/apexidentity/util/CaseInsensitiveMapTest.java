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
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Paul C. Bryan
 */
public class CaseInsensitiveMapTest {

    private CaseInsensitiveMap<String> map; // map to perform tests on
    private String upper; // key as an upper-case string
    private String lower; // key as a lower-case string
    private String value1; // value to store and check for in mapping
    private String value2; // value to replace original with to see that replacement sticks
    private String spurious; // key to use to test spurious lookups

    // ----- context -----------------------------------------------------------

    @BeforeMethod
    public void before() {
        map = new CaseInsensitiveMap<String>(new HashMap<String, String>());
        upper = "AAA-" + UUID.randomUUID().toString().toUpperCase() + "-BBB";
        lower = upper.toLowerCase();
        value1 = UUID.randomUUID().toString();
        value2 = UUID.randomUUID().toString();
        spurious = "aaa-" + UUID.randomUUID().toString() + "-BBB";
    }

    // ----- upper case --------------------------------------------------------

    @Test
    public void upperPut_keyRetainsCase() {
        map.put(upper, value1);
        assertThat(map.keySet().iterator().next()).isEqualTo(upper);
    }

    @Test
    public void upperPut_upperGet() {
        map.put(upper, value1);
        assertThat(map.get(upper)).isEqualTo(value1);
    }

    @Test
    public void upperPut_upperContains() {
        map.put(upper, value1);
        assertThat(map.containsKey(upper)).isTrue();
    }

    @Test
    public void upperPut_spuriousContains() {
        map.put(upper, value1);
        assertThat(map.containsKey(spurious)).isFalse();
    }

    @Test
    public void upperPut_spuriousGet() {
        map.put(upper, value1);
        assertThat(map.get(spurious)).isNull();
    }

    @Test
    public void upperPut_upperRemove_upperGet() {
        map.put(upper, value1);
        map.remove(upper);
        assertThat(map.get(upper)).isNull();
    }

    // ----- lower case --------------------------------------------------------

    @Test
    public void lowerPut_keyRetainsCase() {
        map.put(lower, value1);
        assertThat(map.keySet().iterator().next()).isEqualTo(lower);
    }

    @Test
    public void lowerPut_lowerGet() {
        map.put(lower, value1);
        assertThat(map.get(lower)).isEqualTo(value1);
    }

    @Test
    public void lowerPut_lowerContains() {
        map.put(lower, value1);
        assertThat(map.containsKey(lower)).isTrue();
    }

    @Test
    public void lowerPut_spuriousContains() {
        map.put(lower, value1);
        assertThat(map.containsKey(spurious)).isFalse();
    }

    @Test
    public void lowerPut_spuriousGet() {
        map.put(lower, value1);
        assertThat(map.get(spurious)).isNull();
    }

    @Test
    public void lowerPut_lowerRemove_lowerGet() {
        map.put(lower, value1);
        map.remove(lower);
        assertThat(map.get(lower)).isNull();
    }

    // ----- upper then lower case ---------------------------------------------

    @Test
    public void upperPut_lowerGet() {
        map.put(upper, value1);
        assertThat(map.get(lower)).isEqualTo(value1);
    }

    @Test
    public void upperPut_lowerContains() {
        map.put(upper, value1);
        assertThat(map.containsKey(lower)).isTrue();
    }

    @Test
    public void upperPut_upperRemove_lowerGet() {
        map.put(upper, value1);
        map.remove(upper);
        assertThat(map.get(lower)).isNull();
    }

    @Test
    public void upperPut_lowerRemove_upperGet() {
        map.put(upper, value1);
        map.remove(lower);
        assertThat(map.get(upper)).isNull();
    }

    @Test
    public void upperPut_lowerRemove_lowerGet() {
        map.put(upper, value1);
        map.remove(lower);
        assertThat(map.get(lower)).isNull();
    }

    @Test
    public void upperPut_lowerPut_upperGet() {
        map.put(upper, value1);
        map.put(lower, value2);
        assertThat(map.get(upper)).isEqualTo(value2);
    }

    @Test
    public void upperPut_lowerPut_lowerGet() {
        map.put(upper, value1);
        map.put(lower, value2);
        assertThat(map.get(lower)).isEqualTo(value2);
    }

    // ----- lower then upper case ---------------------------------------------

    @Test
    public void lowerPut_upperGet() {
        map.put(lower, value1);
        assertThat(map.get(upper)).isEqualTo(value1);
    }

    @Test
    public void lowerPut_upperContains() {
        map.put(lower, value1);
        assertThat(map.containsKey(upper)).isTrue();
    }

    @Test
    public void lowerPut_lowerRemove_upperGet() {
        map.put(lower, value1);
        map.remove(lower);
        assertThat(map.get(upper)).isNull();
    }

    @Test
    public void lowerPut_upperRemove_lowerGet() {
        map.put(lower, value1);
        map.remove(upper);
        assertThat(map.get(lower)).isNull();
    }

    @Test
    public void lowerPut_upperRemove_upperGet() {
        map.put(lower, value1);
        map.remove(upper);
        assertThat(map.get(upper)).isNull();
    }

    @Test
    public void lowerPut_upperPut_lowerGet() {
        map.put(lower, value1);
        map.put(upper, value2);
        assertThat(map.get(lower)).isEqualTo(value2);
    }

    @Test
    public void lowerPut_upperPut_upperGet() {
        map.put(lower, value1);
        map.put(upper, value2);
        assertThat(map.get(upper)).isEqualTo(value2);
    }
}
