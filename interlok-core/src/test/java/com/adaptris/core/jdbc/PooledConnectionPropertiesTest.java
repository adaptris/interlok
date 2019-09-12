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

package com.adaptris.core.jdbc;

import static org.junit.Assert.assertEquals;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class PooledConnectionPropertiesTest {

  private static final Map<Class, DummyValue> DUMMY_VALUES;
  private static final GuidGenerator GUID = new GuidGenerator();

  static {
    Map<Class, DummyValue> map = new HashMap<>();
    map.put(int.class, () -> "10");
    map.put(Integer.class, () -> "10");
    map.put(Boolean.class, () -> "false");
    map.put(boolean.class, () -> "false");
    map.put(String.class, () -> GUID.safeUUID());
    map.put(float.class, () -> "1.0");
    map.put(Float.class, () -> "1.0");
    map.put(double.class, () -> "2.0");
    map.put(Double.class, () -> "2.0");
    map.put(long.class, () -> "1000");
    map.put(Long.class, () -> "1000");
    DUMMY_VALUES = Collections.unmodifiableMap(map);
  }

  @FunctionalInterface
  private interface DummyValue {
    String value();
  }

  @Test
  public void testApply() throws Exception {
    String url = "jdbc:derby:memory:" + GUID.safeUUID() + ";create=true";
    String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    KeyValuePairSet props = new KeyValuePairSet();
    for (PooledConnectionProperties p : PooledConnectionProperties.values()) {
      props.add(new KeyValuePair(p.name(), DUMMY_VALUES.get(p.propertyType()).value()));
    }
    props.add(new KeyValuePair("hello", "world"));
    ComboPooledDataSource ds = new ComboPooledDataSource();
    PooledConnectionProperties.apply(null, ds);
    PooledConnectionProperties.apply(props, ds);
    assertEquals(10, ds.getAcquireIncrement());
  }

}
