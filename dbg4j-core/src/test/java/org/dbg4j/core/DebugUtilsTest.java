

package org.dbg4j.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import org.dbg4j.core.annotations.Adapter;
import org.dbg4j.core.annotations.Debug;
import org.dbg4j.core.annotations.Ignore;
import org.dbg4j.core.beans.DebugData;

import static org.junit.Assert.*;

public class DebugUtilsTest {

    @Test
    public void testGetClassName() throws Exception {
        assertEquals("NULL", DebugUtils.getClassName(null));
        assertEquals("org.dbg4j.core.DebugUtilsTest", DebugUtils.getClassName(new DebugUtilsTest()));
    }

    @Test
    public void testGetMethodSignature() throws Exception {
        String signature = DebugUtils.getMethodSignature(TestClass.class.getDeclaredMethod
                ("method1ForSignature", String.class, Integer.class));
        assertEquals("String method1ForSignature(String, Integer)", signature);

        signature = DebugUtils.getMethodSignature(TestClass.class.getDeclaredMethod
                ("method2ForSignature"));
        assertEquals("void method2ForSignature()", signature);
    }

    @Test
    public void testGetFieldValues() throws Exception {
        Map<String, String> fields =  DebugUtils.getFieldValues(new TestClass(),
                new HashSet<Field>(Arrays.asList(TestClass.class.getDeclaredFields())));

        assertNotNull(fields);
        assertEquals(4, fields.size());
        assertEquals("100", fields.get("field1"));
        assertEquals("null", fields.get("field2"));
        Assert.assertEquals(CustomEvaluationAdapter.VALUE, fields.get("field3"));
        assertEquals("3.1415", fields.get("PI"));
    }

    @Test
    public void testContainsAnnotation() throws Exception {
        assertFalse(DebugUtils.containsAnnotation(null, Ignore.class));

        Annotation[] annotations = TestClass.class.getDeclaredField("field3").getAnnotations();
        assertTrue(DebugUtils.containsAnnotation(annotations, Ignore.class));
        assertFalse(DebugUtils.containsAnnotation(annotations, Debug.class));
    }

    @Test
    public void testGetAnnotation() throws Exception {
        assertNull(DebugUtils.getAnnotation(null, Ignore.class));

        Annotation[] annotations = TestClass.class.getDeclaredField("field3").getAnnotations();
        Ignore ignoreAnnotation = DebugUtils.getAnnotation(annotations, Ignore.class);

        assertNotNull(ignoreAnnotation);
    }

    @Test
    public void testGetObjectFields() throws Exception {
        Set<Field> fields = DebugUtils.getObjectFields(null, Ignore.class, true);
        assertNotNull(fields);
        assertEquals(0, fields.size());

        fields = DebugUtils.getObjectFields(new TestClass(), Ignore.class, true);
        assertEquals(1, fields.size());
        assertTrue(fields.contains(TestClass.class.getDeclaredField("field3")));

        fields = DebugUtils.getObjectFields(new TestClass(), Ignore.class, false);
        assertEquals(3, fields.size());
        assertFalse(fields.contains(TestClass.class.getDeclaredField("field3")));
        assertTrue(fields.contains(TestClass.class.getDeclaredField("field1")));
        assertTrue(fields.contains(TestClass.class.getDeclaredField("field2")));
        assertTrue(fields.contains(TestClass.class.getDeclaredField("PI")));
    }

    @Test
    public void testGetObjectFieldsOverloaded() throws Exception {
        String[] fieldNames = new String[]{"field1", "PI"};

        Set<Field> fields = DebugUtils.getObjectFields(null, fieldNames);
        assertNotNull(0);
        assertEquals(0, fields.size());

        fields = DebugUtils.getObjectFields(new TestClass(), new String[]{});
        assertNotNull(0);
        assertEquals(0, fields.size());

        fields = DebugUtils.getObjectFields(new TestClass(), fieldNames);
        assertEquals(fieldNames.length, fields.size());
        assertTrue(fields.contains(TestClass.class.getDeclaredField("field1")));
        assertTrue(fields.contains(TestClass.class.getDeclaredField("PI")));
    }

    @Test
    public void testToJsonArray() throws Exception {
        DebugData dd1 = new DebugData("key1", "val1");
        DebugData dd2 = new DebugData("key2", "val2");
        DebugData dd3 = new DebugData("key3", "val3");
        dd3.set("key3.1", "val3.1");

        String result = DebugUtils.toJsonArray(Arrays.asList(dd1, dd2, dd3));

        JSONArray jsonArray = new JSONArray(result);

        assertEquals(3, jsonArray.length());

        JSONObject json = jsonArray.getJSONObject(0);
        assertEquals("val1", json.getString("key1"));

        json = jsonArray.getJSONObject(1);
        assertEquals("val2", json.getString("key2"));

        json = jsonArray.getJSONObject(2);
        assertEquals("val3", json.getString("key3"));
        assertEquals("val3.1", json.getString("key3.1"));
    }
}

class TestClass {
    private int field1 = 100;

    private String field2;

    @Adapter(value = CustomEvaluationAdapter.class)
    @Ignore
    private double field3;

    private static final double PI = 3.1415;


    private String method1ForSignature(String v1, Integer v2){
        return "";
    }

    private void method2ForSignature(){}
}

