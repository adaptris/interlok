/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

public class SimpleBeanUtilTest extends SimpleBeanUtil {

  @Test
  public void testPrimtivePrimitives() {
    MyPrimitiveBean bean = new MyPrimitiveBean();
    assertTrue(SimpleBeanUtil.callSetter(bean, "SetLongParam", "1"));
    assertTrue(SimpleBeanUtil.callSetter(bean, "setDoubleParam", "1"));
    assertTrue(SimpleBeanUtil.callSetter(bean, "setStringParam", "1"));
    assertTrue(SimpleBeanUtil.callSetter(bean, "setBooleanParam", "yes"));
    assertTrue(SimpleBeanUtil.callSetter(bean, "setIntParam", "1"));
    assertTrue(SimpleBeanUtil.callSetter(bean, "setFloatParam", "1"));
    assertFalse(SimpleBeanUtil.callSetter(bean, "blah blah blah", "1"));
    assertFalse(SimpleBeanUtil.callSetter(bean, "setMyObject", "1"));
    assertFalse(SimpleBeanUtil.callSetter(bean, "setMultiParam", "1"));
    
    assertEquals(1L, bean.getLongParam());
    assertEquals(Double.parseDouble("1"), bean.getDoubleParam(), 0.1);
    assertEquals("1", bean.getStringParam());
    assertEquals(true, bean.getBooleanParam());
    assertEquals(1, bean.getIntParam());
    assertEquals(Float.parseFloat("1"), bean.getFloatParam(), 0.1);
  }

  @Test
  public void testObjectPrimitives() {
    MyObjectPrimitiveBean bean = new MyObjectPrimitiveBean();
    assertTrue(SimpleBeanUtil.callSetter(bean, "setLongParam", "1"));
    assertTrue(SimpleBeanUtil.callSetter(bean, "setDoubleParam", "1"));
    assertTrue(SimpleBeanUtil.callSetter(bean, "setStringParam", "1"));
    assertTrue(SimpleBeanUtil.callSetter(bean, "setBooleanParam", "yes"));
    assertTrue(SimpleBeanUtil.callSetter(bean, "setIntParam", "1"));
    assertTrue(SimpleBeanUtil.callSetter(bean, "setFloatParam", "1"));
    assertFalse(SimpleBeanUtil.callSetter(bean, "blah blah blah", "1"));
    assertFalse(SimpleBeanUtil.callSetter(bean, "setMyObject", "1"));
    assertFalse(SimpleBeanUtil.callSetter(bean, "setMultiParam", "1"));

    assertEquals(Long.valueOf(1L), bean.getLongParam());
    assertEquals(Double.parseDouble("1"), bean.getDoubleParam(), 0.1);
    assertEquals("1", bean.getStringParam());
    assertEquals(true, bean.getBooleanParam());
    assertEquals(Integer.valueOf(1), bean.getIntParam());
    assertEquals(Float.parseFloat("1"), bean.getFloatParam(), 0.1);
  }

  @Test
  public void testGettersAsMap() throws Exception {
    MyObjectPrimitiveBean bean = new MyObjectPrimitiveBean();
    Map<String, Method> getters = gettersAsMap(bean.getClass());
    // Discards getInstance + getClass at least, so 7 traditional getter+setters.
    assertEquals(7, getters.size());
    assertTrue(getters.containsKey("GETSTRINGPARAM"));
  }

  public class MyPrimitiveBean {
    @Getter
    @Setter
    private long longParam;
    @Getter
    @Setter
    private double doubleParam;
    @Getter
    @Setter
    private String stringParam;
    @Getter
    @Setter
    private boolean booleanParam;
    @Getter
    @Setter
    private int intParam;
    @Getter
    @Setter
    private float floatParam;
    @Getter
    @Setter
    private Object myObject;

    public void setMultiParam(String a, String b) {

    }

  }

  public static class MyObjectPrimitiveBean {
    @Getter
    @Setter
    private Long longParam;
    @Getter
    @Setter
    private Double doubleParam;
    @Getter
    @Setter
    private String stringParam;
    @Getter
    @Setter
    private Boolean booleanParam;
    @Getter
    @Setter
    private Integer intParam;
    @Getter
    @Setter
    private Float floatParam;
    @Getter
    @Setter
    private Object myObject;

    public void setMultiParam(String a, String b) {

    }

    static MyObjectPrimitiveBean getInstance() {
      return new MyObjectPrimitiveBean();
    }
  }
}
