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

import org.junit.Test;

public class SimpleBeanUtilTest {

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

  public class MyPrimitiveBean {
    private long longParam;
    private double doubleParam;
    private String stringParam;
    private boolean booleanParam;
    private int intParam;
    private float floatParam;
    private Object myObject;

    public long getLongParam() {
      return longParam;
    }

    public void setLongParam(long longParam) {
      this.longParam = longParam;
    }

    public double getDoubleParam() {
      return doubleParam;
    }

    public void setDoubleParam(double doubleParam) {
      this.doubleParam = doubleParam;
    }

    public String getStringParam() {
      return stringParam;
    }

    public void setStringParam(String stringParam) {
      this.stringParam = stringParam;
    }

    public boolean getBooleanParam() {
      return booleanParam;
    }

    public void setBooleanParam(boolean booleanParam) {
      this.booleanParam = booleanParam;
    }

    public int getIntParam() {
      return intParam;
    }

    public void setIntParam(int intParam) {
      this.intParam = intParam;
    }

    public float getFloatParam() {
      return floatParam;
    }

    public void setFloatParam(float floatParam) {
      this.floatParam = floatParam;
    }

    public Object getMyObject() {
      return myObject;
    }

    public void setMyObject(Object myObject) {
      this.myObject = myObject;
    }

    public void setMultiParam(String a, String b) {

    }

  }

  public class MyObjectPrimitiveBean {
    private Long longParam;
    private Double doubleParam;
    private String stringParam;
    private Boolean booleanParam;
    private Integer intParam;
    private Float floatParam;
    private Object myObject;

    public Long getLongParam() {
      return longParam;
    }

    public void setLongParam(Long longParam) {
      this.longParam = longParam;
    }

    public Double getDoubleParam() {
      return doubleParam;
    }

    public void setDoubleParam(Double doubleParam) {
      this.doubleParam = doubleParam;
    }

    public String getStringParam() {
      return stringParam;
    }

    public void setStringParam(String stringParam) {
      this.stringParam = stringParam;
    }

    public Boolean getBooleanParam() {
      return booleanParam;
    }

    public void setBooleanParam(Boolean booleanParam) {
      this.booleanParam = booleanParam;
    }

    public Integer getIntParam() {
      return intParam;
    }

    public void setIntParam(Integer intParam) {
      this.intParam = intParam;
    }

    public Float getFloatParam() {
      return floatParam;
    }

    public void setFloatParam(Float floatParam) {
      this.floatParam = floatParam;
    }

    public Object getMyObject() {
      return myObject;
    }

    public void setMyObject(Object myObject) {
      this.myObject = myObject;
    }

    public void setMultiParam(String a, String b) {

    }

  }
}
