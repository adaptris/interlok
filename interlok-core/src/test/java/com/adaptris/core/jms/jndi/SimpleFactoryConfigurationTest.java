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

package com.adaptris.core.jms.jndi;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import javax.jms.XAConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.adaptris.core.BaseCase;
import com.adaptris.interlok.util.Closer;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class SimpleFactoryConfigurationTest extends BaseCase {

  private static final String SOME_STRING = "string";
  private static final int INT_VALUE_1 = 1;
  private static final long LONG_VALUE_1 = 1L;
  private static final double DOUBLE_VALUE_1 = 1.0d;
  private static final float FLOAT_VALUE_1 = 1.0f;

  private static final int INT_VALUE_0 = 0;
  private static final long LONG_VALUE_0 = 0L;
  private static final double DOUBLE_VALUE_0 = 0.0d;
  private static final float FLOAT_VALUE_0 = 0.0f;

  private static final String SOME_INT_VALUE = "SomeIntValue";
  private static final String SOME_LONG_VALUE = "SomeLongValue";
  private static final String SOME_STRING_VALUE = "SomeStringValue";
  private static final String SOME_BOOLEAN_VALUE = "SomeBooleanValue";
  private static final String SOME_FLOAT_VALUE = "SomeFloatValue";
  private static final String SOME_DOUBLE_VALUE = "SomeDoubleValue";
  private static final String TWO_VALUES_TOGETHER = "TwoValuesTogether";
  private static final String SOME_OBJECT_VALUE = "SomeObjectValue";

  private static final String SOME_BOOLEAN_OBJ_VALUE = "someBooleanObj";
  private static final String SOME_DOUBLE_OBJ_VALUE = "someDoubleObj";
  private static final String SOME_INTEGER_OBJ_VALUE = "someIntegerObj";
  private static final String SOME_LONG_OBJ_VALUE = "someLongObj";
  private static final String SOME_FLOAT_OBJ_VALUE = "someFloatObj";

  @Mock private XAConnectionFactory mockXAConnectionFactory;
  private AutoCloseable openMocks;

  @Before
  public void setUp() throws Exception {
    openMocks = MockitoAnnotations.openMocks(this);
  }

  @After
  public void tearDown() throws Exception {
    Closer.closeQuietly(openMocks);
  }
  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testApplyTopicConnectionFactoryConfiguration() throws Exception {
    SimpleFactoryConfiguration extras = createBase();
    DummyConnectionFactory mycf = new DummyConnectionFactory();
    extras.applyConfiguration(mycf);
    doModifiedAssertions(mycf);
  }

  @Test
  public void testApplyNonConnectionFactoryConfiguration() throws Exception {
    SimpleFactoryConfiguration extras = createBase();
    try {
      extras.applyConfiguration(new Object());
      fail("Should fail, not given a XA/ConnectionFactory.");
    } catch (Exception ex) {
      //expected
    }
  }

  @Test
  public void testApplyXAConnectionFactoryConfigurationNoError() throws Exception {
    SimpleFactoryConfiguration extras = createBase();
    extras.applyConfiguration(mockXAConnectionFactory);
  }

  @Test
  public void testApplyQueueConnectionFactoryConfiguration() throws Exception {
    SimpleFactoryConfiguration extras = createBase();
    DummyConnectionFactory mycf = new DummyConnectionFactory();
    extras.applyConfiguration(mycf);
    doModifiedAssertions(mycf);
  }

  @Test
  public void testApplyTopicConnectionFactory_NonInteger() throws Exception {
    SimpleFactoryConfiguration extras = create(new KeyValuePair(SOME_INT_VALUE, "fred"));
    DummyConnectionFactory mycf = new DummyConnectionFactory();
    extras.applyConfiguration(mycf);
    doBaseAssertions(mycf);
  }

  @Test
  public void testApplyQueueConnectionFactory_NonInteger() throws Exception {
    SimpleFactoryConfiguration extras = create(new KeyValuePair(SOME_INT_VALUE, "fred"));
    DummyConnectionFactory mycf = new DummyConnectionFactory();
    extras.applyConfiguration(mycf);
    doBaseAssertions(mycf);
  }

  @Test
  public void testApplyTopicConnectionFactory_NonLong() throws Exception {
    SimpleFactoryConfiguration extras = create(new KeyValuePair(SOME_LONG_VALUE, "fred"));
    DummyConnectionFactory mycf = new DummyConnectionFactory();
    extras.applyConfiguration(mycf);
    doBaseAssertions(mycf);
  }

  @Test
  public void testApplyQueueConnectionFactory_NonLong() throws Exception {
    SimpleFactoryConfiguration extras = create(new KeyValuePair(SOME_LONG_VALUE, "fred"));
    DummyConnectionFactory mycf = new DummyConnectionFactory();
    extras.applyConfiguration(mycf);
    doBaseAssertions(mycf);
  }

  @Test
  public void testApplyTopicConnectionFactory_NonBoolean() throws Exception {
    SimpleFactoryConfiguration extras = create(new KeyValuePair(SOME_BOOLEAN_VALUE, "fred"));
    DummyConnectionFactory mycf = new DummyConnectionFactory();
    extras.applyConfiguration(mycf);
    doBaseAssertions(mycf);
  }

  @Test
  public void testApplyQueueConnectionFactory_NonBoolean() throws Exception {
    SimpleFactoryConfiguration extras = create(new KeyValuePair(SOME_BOOLEAN_VALUE, "fred"));
    DummyConnectionFactory mycf = new DummyConnectionFactory();
    extras.applyConfiguration(mycf);
    doBaseAssertions(mycf);
  }

  @Test
  public void testApplyTopicConnectionFactory_NonFloat() throws Exception {
    SimpleFactoryConfiguration extras = create(new KeyValuePair(SOME_FLOAT_VALUE, "fred"));
    DummyConnectionFactory mycf = new DummyConnectionFactory();
    extras.applyConfiguration(mycf);
    doBaseAssertions(mycf);
  }

  @Test
  public void testApplyQueueConnectionFactory_NonFloat() throws Exception {
    SimpleFactoryConfiguration extras = create(new KeyValuePair(SOME_FLOAT_VALUE, "fred"));
    DummyConnectionFactory mycf = new DummyConnectionFactory();
    extras.applyConfiguration(mycf);
    doBaseAssertions(mycf);
  }

  @Test
  public void testApplyTopicConnectionFactory_NonDouble() throws Exception {
    SimpleFactoryConfiguration extras = create(new KeyValuePair(SOME_DOUBLE_VALUE, "fred"));
    DummyConnectionFactory mycf = new DummyConnectionFactory();
    extras.applyConfiguration(mycf);
    doBaseAssertions(mycf);
  }

  @Test
  public void testApplyQueueConnectionFactory_NonDouble() throws Exception {
    SimpleFactoryConfiguration extras = create(new KeyValuePair(SOME_DOUBLE_VALUE, "fred"));
    DummyConnectionFactory mycf = new DummyConnectionFactory();
    extras.applyConfiguration(mycf);
    doBaseAssertions(mycf);
  }

  @Test
  public void testApplyTopicConnectionFactory_NoSetter() throws Exception {
    SimpleFactoryConfiguration extras = create(new KeyValuePair("HelloThere", "fred"));
    DummyConnectionFactory mycf = new DummyConnectionFactory();
    extras.applyConfiguration(mycf);
    doBaseAssertions(mycf);
  }

  @Test
  public void testApplyQueueConnectionFactory_NoSetter() throws Exception {
    SimpleFactoryConfiguration extras = create(new KeyValuePair("HelloThere", "fred"));
    DummyConnectionFactory mycf = new DummyConnectionFactory();
    extras.applyConfiguration(mycf);
    doBaseAssertions(mycf);
  }

  @Test
  public void testApplyTopicConnectionFactory_Object() throws Exception {
    SimpleFactoryConfiguration extras = create(new KeyValuePair(SOME_OBJECT_VALUE, "fred"));
    DummyConnectionFactory mycf = new DummyConnectionFactory();
    extras.applyConfiguration(mycf);
    doBaseAssertions(mycf);
  }

  @Test
  public void testApplyQueueConnectionFactory_Object() throws Exception {
    SimpleFactoryConfiguration extras = create(new KeyValuePair(SOME_OBJECT_VALUE, "fred"));
    DummyConnectionFactory mycf = new DummyConnectionFactory();
    extras.applyConfiguration(mycf);
    doBaseAssertions(mycf);
  }

  @Test
  public void testApplyTopicConnectionFactory_TwoParams() throws Exception {
    SimpleFactoryConfiguration extras = create(new KeyValuePair(TWO_VALUES_TOGETHER, "fred"));
    DummyConnectionFactory mycf = new DummyConnectionFactory();
    extras.applyConfiguration(mycf);
    doBaseAssertions(mycf);
  }

  @Test
  public void testApplyQueueConnectionFactory_TwoParams() throws Exception {
    SimpleFactoryConfiguration extras = create(new KeyValuePair(TWO_VALUES_TOGETHER, "fred"));
    DummyConnectionFactory mycf = new DummyConnectionFactory();
    extras.applyConfiguration(mycf);
    doBaseAssertions(mycf);
  }

  private void doModifiedAssertions(DummyConnectionFactory dummy) {
    assertEquals(INT_VALUE_1, dummy.getSomeIntValue());
    assertEquals(LONG_VALUE_1, dummy.getSomeLongValue());
    assertEquals(Boolean.TRUE.booleanValue(), dummy.getSomeBooleanValue());
    assertEquals(SOME_STRING, dummy.getSomeStringValue());
    assertEquals(FLOAT_VALUE_1, dummy.getSomeFloatValue(), 0.1f);
    assertEquals(DOUBLE_VALUE_1, dummy.getSomeDoubleValue(), 0.1);
    assertNull(dummy.getSomeObjectValue());
    assertEquals(Integer.valueOf(INT_VALUE_1), dummy.getSomeIntegerObj());
    assertEquals(Long.valueOf(LONG_VALUE_1), dummy.getSomeLongObj());
    assertEquals(Boolean.valueOf(true), dummy.getSomeBooleanObj());
    assertEquals(Double.valueOf(DOUBLE_VALUE_1), dummy.getSomeDoubleObj());
    assertEquals(Float.valueOf(FLOAT_VALUE_1), dummy.getSomeFloatObj());
  }

  private void doBaseAssertions(DummyConnectionFactory dummy) {
    assertEquals(INT_VALUE_0, dummy.getSomeIntValue());
    assertEquals(LONG_VALUE_0, dummy.getSomeLongValue());
    assertEquals(Boolean.FALSE.booleanValue(), dummy.getSomeBooleanValue());
    assertEquals(DummyConnectionFactory.class.getSimpleName(), dummy.getSomeStringValue());
    assertEquals(FLOAT_VALUE_0, dummy.getSomeFloatValue(), 0.1f);
    assertEquals(DOUBLE_VALUE_0, dummy.getSomeDoubleValue(), 0.1);
    assertNull(dummy.getSomeObjectValue());
    assertNull(dummy.getSomeIntegerObj());
    assertNull(dummy.getSomeLongObj());
    assertNull(dummy.getSomeBooleanObj());
    assertNull(dummy.getSomeDoubleObj());
    assertNull(dummy.getSomeFloatObj());
  }

  private SimpleFactoryConfiguration createBase() {
    SimpleFactoryConfiguration result = new SimpleFactoryConfiguration();
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair(SOME_INT_VALUE, String.valueOf(INT_VALUE_1)));
    kvps.add(new KeyValuePair(SOME_LONG_VALUE, String.valueOf(LONG_VALUE_1)));
    kvps.add(new KeyValuePair(SOME_STRING_VALUE, SOME_STRING));
    kvps.add(new KeyValuePair(SOME_BOOLEAN_VALUE, Boolean.TRUE.toString()));
    kvps.add(new KeyValuePair(SOME_FLOAT_VALUE, String.valueOf(FLOAT_VALUE_1)));
    kvps.add(new KeyValuePair(SOME_DOUBLE_VALUE, String.valueOf(DOUBLE_VALUE_1)));

    kvps.add(new KeyValuePair(SOME_INTEGER_OBJ_VALUE, String.valueOf(INT_VALUE_1)));
    kvps.add(new KeyValuePair(SOME_LONG_OBJ_VALUE, String.valueOf(LONG_VALUE_1)));
    kvps.add(new KeyValuePair(SOME_DOUBLE_OBJ_VALUE, String.valueOf(DOUBLE_VALUE_1)));
    kvps.add(new KeyValuePair(SOME_FLOAT_OBJ_VALUE, String.valueOf(FLOAT_VALUE_1)));
    kvps.add(new KeyValuePair(SOME_BOOLEAN_OBJ_VALUE, Boolean.TRUE.toString()));


    result.setProperties(kvps);
    return result;
  }

  protected static SimpleFactoryConfiguration create(KeyValuePair kvp) {
    SimpleFactoryConfiguration result = new SimpleFactoryConfiguration();
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(kvp);
    result.setProperties(kvps);
    return result;
  }

  // Purely for testing getters and setters; I suspect I could use mockito to do this.
  // But I haven't found a connectionFactory impl that *actually has* double/float setters
  public class DummyConnectionFactory extends ActiveMQConnectionFactory {
    private int someIntValue = INT_VALUE_0;
    private long someLongValue = LONG_VALUE_0;
    private String someStringValue = DummyConnectionFactory.class.getSimpleName();
    private boolean someBooleanValue = false;
    private float someFloatValue = FLOAT_VALUE_0;
    private double someDoubleValue = DOUBLE_VALUE_0;
    private Object someObjectValue = null;
    private Boolean someBooleanObj;
    private Double someDoubleObj;
    private Integer someIntegerObj;
    private Long someLongObj;
    private Float someFloatObj;

    public DummyConnectionFactory() {
      super();
    }

    public int getSomeIntValue() {
      return someIntValue;
    }

    public void setSomeIntValue(int someIntValue) {
      this.someIntValue = someIntValue;
    }

    public long getSomeLongValue() {
      return someLongValue;
    }

    public void setSomeLongValue(long someLongValue) {
      this.someLongValue = someLongValue;
    }

    public String getSomeStringValue() {
      return someStringValue;
    }

    public void setSomeStringValue(String someStringValue) {
      this.someStringValue = someStringValue;
    }

    public boolean getSomeBooleanValue() {
      return someBooleanValue;
    }

    public void setSomeBooleanValue(boolean someBooleanValue) {
      this.someBooleanValue = someBooleanValue;
    }

    public float getSomeFloatValue() {
      return someFloatValue;
    }

    public void setSomeFloatValue(float someFloatValue) {
      this.someFloatValue = someFloatValue;
    }

    public double getSomeDoubleValue() {
      return someDoubleValue;
    }

    public void setSomeDoubleValue(double someDoubleValue) {
      this.someDoubleValue = someDoubleValue;
    }

    public Object getSomeObjectValue() {
      return someObjectValue;
    }

    public void setSomeObjectValue(Object someObjectValue) {
      this.someObjectValue = someObjectValue;
    }

    public void setTwoValuesTogether(String s, int i) {
      someStringValue = s;
      someIntValue = i;
    }

    public Boolean getSomeBooleanObj() {
      return someBooleanObj;
    }

    public void setSomeBooleanObj(Boolean someBooleanObj) {
      this.someBooleanObj = someBooleanObj;
    }

    public Double getSomeDoubleObj() {
      return someDoubleObj;
    }

    public void setSomeDoubleObj(Double someDoubleObj) {
      this.someDoubleObj = someDoubleObj;
    }

    public Integer getSomeIntegerObj() {
      return someIntegerObj;
    }

    public void setSomeIntegerObj(Integer someIntegerObj) {
      this.someIntegerObj = someIntegerObj;
    }

    public Long getSomeLongObj() {
      return someLongObj;
    }

    public void setSomeLongObj(Long someLongObj) {
      this.someLongObj = someLongObj;
    }

    public Float getSomeFloatObj() {
      return someFloatObj;
    }

    public void setSomeFloatObj(Float someFloatObj) {
      this.someFloatObj = someFloatObj;
    }
  }

}
