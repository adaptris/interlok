/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.adaptris.core.services.dynamic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.TradingRelationship;

@SuppressWarnings("deprecation")
public class ServiceNameMapperTest {

  @Test
  public void testEqualsHashCode() {
    ServiceNameMapper m1 = new ServiceNameMapper("src", "dst", "type", "");
    ServiceNameMapper m2 = new ServiceNameMapper("src", "dst", "type", "");
    assertEquals(m1, m2);
    assertEquals(m1.hashCode(), m2.hashCode());
    assertTrue(m1.equals(m1));
    assertFalse(m1.equals(null));
    assertFalse(m1.equals(new Object()));
  }



  @Test(expected = IllegalArgumentException.class)
  public void testSetTradingRelationship() {
    ServiceNameMapper mapper = new ServiceNameMapper();
    mapper.setTradingRelationship(null);
  }

  @Test
  public void testRetrieveDefaultNameFromNullServiceName() {
    ServiceNameMapper mapper = new ServiceNameMapper();
    assertEquals("*-*-*", mapper.getServiceName());
    TradingRelationship serviceId = new TradingRelationship("src", "dest", "type");
    mapper.setServiceName(null);
    mapper.setTradingRelationship(serviceId);
    assertEquals("src-dest-type", mapper.getServiceName());
  }

  @Test
  public void testRetrieveDefaultNameFromEmptyServiceName() {
    ServiceNameMapper mapper = new ServiceNameMapper();
    assertEquals("*-*-*", mapper.getServiceName());
    TradingRelationship serviceId = new TradingRelationship("src", "dest", "type");
    mapper.setServiceName("");
    mapper.setTradingRelationship(serviceId);
    assertEquals("src-dest-type", mapper.getServiceName());
  }

  @Test
  public void testRetrieveConfiguredName() {
    ServiceNameMapper mapper = new ServiceNameMapper();
    TradingRelationship serviceId = new TradingRelationship("src", "dest", "type");
    mapper.setTradingRelationship(serviceId);
    mapper.setServiceName("over-ride");
    assertEquals("over-ride", mapper.getServiceName());
  }

  @Test
  public void testBug886() throws Exception {
    AdaptrisMarshaller cm = DefaultMarshaller.getDefaultMarshaller();
    ServiceNameMapper m = new ServiceNameMapper("src", "dst", "type", "logicalName");
    String xml = cm.marshal(m);
    ServiceNameMapper result = (ServiceNameMapper) cm.unmarshal(xml);
    assertEquals(m.getServiceName(), result.getServiceName());
  }

}
