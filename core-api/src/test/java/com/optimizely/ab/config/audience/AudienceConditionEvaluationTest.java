/**
 *
 *    Copyright 2016-2017, Optimizely and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.optimizely.ab.config.audience;

import com.optimizely.ab.config.Experiment;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the evaluation of different audience condition types (And, Or, Not, and UserAttribute)
 */
@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
    justification = "mockito verify calls do have a side-effect")
public class AudienceConditionEvaluationTest {

    Map<String, String> testUserAttributes;
    Map<String, Object> testTypedUserAttributes;

    @Before
    public void initialize() {
        testUserAttributes = new HashMap<String, String>();
        testUserAttributes.put("browser_type", "chrome");
        testUserAttributes.put("device_type", "Android");

        testTypedUserAttributes = new HashMap<String, Object>();
        testTypedUserAttributes.put("is_firefox", true);
        testTypedUserAttributes.put("num_counts", 3.55);
        testTypedUserAttributes.put("num_size", 3);
        testTypedUserAttributes.put("meta_data", testUserAttributes);
    }

    /**
     * Verify that UserAttribute.evaluate returns true on exact-matching visitor attribute data.
     */
    @Test
    public void userAttributeEvaluateTrue() throws Exception {
        UserAttribute testInstance = new UserAttribute("browser_type", "custom_dimension", "chrome");
        assertTrue(testInstance.evaluate(testUserAttributes));
    }


    /**
     * Verify that UserAttribute.evaluate returns true on exact-matching visitor attribute data.
     */
    @Test
    public void typedUserAttributeEvaluateTrue() throws Exception {
        UserAttribute testInstance = new UserAttribute("meta_data", "custom_dimension", testUserAttributes);
        UserAttribute testInstance2 = new UserAttribute("is_firefox", "custom_dimension", true);
        UserAttribute testInstance3 = new UserAttribute("num_counts", "custom_dimension", 3.55);
        UserAttribute testInstance4 = new UserAttribute("num_size", "custom_dimension", 3);

        assertTrue(testInstance.evaluate(testTypedUserAttributes));
        assertTrue(testInstance2.evaluate(testTypedUserAttributes));
        assertTrue(testInstance3.evaluate(testTypedUserAttributes));
        assertTrue(testInstance4.evaluate(testTypedUserAttributes));
    }

    /**
     * Verify that UserAttribute.evaluate returns false on non-exact-matching visitor attribute data.
     */
    @Test
    public void userAttributeEvaluateFalse() throws Exception {
        UserAttribute testInstance = new UserAttribute("browser_type", "custom_dimension", "firefox");
        assertFalse(testInstance.evaluate(testUserAttributes));
    }

    /**
     * Verify that UserAttribute.evaluate returns false on unknown visitor attributes.
     */
    @Test
    public void userAttributeUnknownAttribute() throws Exception {
        UserAttribute testInstance = new UserAttribute("unknown_dim", "custom_dimension", "unknown");
        assertFalse(testInstance.evaluate(testUserAttributes));
    }

    /**
     * Verify that NotCondition.evaluate returns true when its condition operand evaluates to false.
     */
    @Test
    public void notConditionEvaluateTrue() throws Exception {
        UserAttribute userAttribute = mock(UserAttribute.class);
        when(userAttribute.evaluate(testUserAttributes)).thenReturn(false);

        NotCondition notCondition = new NotCondition(userAttribute);
        assertTrue(notCondition.evaluate(testUserAttributes));
        verify(userAttribute, times(1)).evaluate(testUserAttributes);
    }

    /**
     * Verify that NotCondition.evaluate returns false when its condition operand evaluates to true.
     */
    @Test
    public void notConditionEvaluateFalse() throws Exception {
        UserAttribute userAttribute = mock(UserAttribute.class);
        when(userAttribute.evaluate(testUserAttributes)).thenReturn(true);

        NotCondition notCondition = new NotCondition(userAttribute);
        assertFalse(notCondition.evaluate(testUserAttributes));
        verify(userAttribute, times(1)).evaluate(testUserAttributes);
    }

    /**
     * Verify that OrCondition.evaluate returns true when at least one of its operand conditions evaluate to true.
     */
    @Test
    public void orConditionEvaluateTrue() throws Exception {
        UserAttribute userAttribute1 = mock(UserAttribute.class);
        when(userAttribute1.evaluate(testUserAttributes)).thenReturn(true);

        UserAttribute userAttribute2 = mock(UserAttribute.class);
        when(userAttribute2.evaluate(testUserAttributes)).thenReturn(false);

        List<Condition> conditions = new ArrayList<Condition>();
        conditions.add(userAttribute1);
        conditions.add(userAttribute2);

        OrCondition orCondition = new OrCondition(conditions);
        assertTrue(orCondition.evaluate(testUserAttributes));
        verify(userAttribute1, times(1)).evaluate(testUserAttributes);
        // shouldn't be called due to short-circuiting in 'Or' evaluation
        verify(userAttribute2, times(0)).evaluate(testUserAttributes);
    }

    /**
     * Verify that OrCondition.evaluate returns false when all of its operand conditions evaluate to false.
     */
    @Test
    public void orConditionEvaluateFalse() throws Exception {
        UserAttribute userAttribute1 = mock(UserAttribute.class);
        when(userAttribute1.evaluate(testUserAttributes)).thenReturn(false);

        UserAttribute userAttribute2 = mock(UserAttribute.class);
        when(userAttribute2.evaluate(testUserAttributes)).thenReturn(false);

        List<Condition> conditions = new ArrayList<Condition>();
        conditions.add(userAttribute1);
        conditions.add(userAttribute2);

        OrCondition orCondition = new OrCondition(conditions);
        assertFalse(orCondition.evaluate(testUserAttributes));
        verify(userAttribute1, times(1)).evaluate(testUserAttributes);
        verify(userAttribute2, times(1)).evaluate(testUserAttributes);
    }

    /**
     * Verify that AndCondition.evaluate returns true when all of its operand conditions evaluate to true.
     */
    @Test
    public void andConditionEvaluateTrue() throws Exception {
        OrCondition orCondition1 = mock(OrCondition.class);
        when(orCondition1.evaluate(testUserAttributes)).thenReturn(true);

        OrCondition orCondition2 = mock(OrCondition.class);
        when(orCondition2.evaluate(testUserAttributes)).thenReturn(true);

        List<Condition> conditions = new ArrayList<Condition>();
        conditions.add(orCondition1);
        conditions.add(orCondition2);

        AndCondition andCondition = new AndCondition(conditions);
        assertTrue(andCondition.evaluate(testUserAttributes));
        verify(orCondition1, times(1)).evaluate(testUserAttributes);
        verify(orCondition2, times(1)).evaluate(testUserAttributes);
    }

    /**
     * Verify that AndCondition.evaluate returns false when any one of its operand conditions evaluate to false.
     */
    @Test
    public void andConditionEvaluateFalse() throws Exception {
        OrCondition orCondition1 = mock(OrCondition.class);
        when(orCondition1.evaluate(testUserAttributes)).thenReturn(false);

        OrCondition orCondition2 = mock(OrCondition.class);
        when(orCondition2.evaluate(testUserAttributes)).thenReturn(true);

        List<Condition> conditions = new ArrayList<Condition>();
        conditions.add(orCondition1);
        conditions.add(orCondition2);

        AndCondition andCondition = new AndCondition(conditions);
        assertFalse(andCondition.evaluate(testUserAttributes));
        verify(orCondition1, times(1)).evaluate(testUserAttributes);
        // shouldn't be called due to short-circuiting in 'And' evaluation
        verify(orCondition2, times(0)).evaluate(testUserAttributes);
    }

    /**
     * Verify that {@link UserAttribute#evaluate(Map)}
     * called when its attribute value is null
     * returns True when the user's attribute value is also null
     *          True when the attribute is not in the map
     *          False when empty string is used.
     * @throws Exception
     */
    @Test
    public void nullValueEvaluate() throws Exception {
        String attributeName = "attribute_name";
        String attributeType = "attribute_type";
        String attributeValue = null;
        UserAttribute nullValueAttribute = new UserAttribute(
                attributeName,
                attributeType,
                attributeValue
        );

        assertTrue(nullValueAttribute.evaluate(Collections.<String, String>emptyMap()));
        assertTrue(nullValueAttribute.evaluate(Collections.singletonMap(attributeName, attributeValue)));
        assertFalse(nullValueAttribute.evaluate((Collections.singletonMap(attributeName, ""))));
    }
}
