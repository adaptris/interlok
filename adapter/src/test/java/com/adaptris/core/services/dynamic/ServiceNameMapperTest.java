package com.adaptris.core.services.dynamic;

import junit.framework.TestCase;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.TradingRelationship;

public class ServiceNameMapperTest extends TestCase {

  public ServiceNameMapperTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testSetTradingRelationship() {
    ServiceNameMapper mapper = new ServiceNameMapper();
    try {
      mapper.setTradingRelationship(null);
      fail("null setTradingRelationship");
    }
    catch (IllegalArgumentException expected) {
    }
  }

  public void testRetrieveDefaultNameFromNullServiceName() {
    ServiceNameMapper mapper = new ServiceNameMapper();
    assertTrue(mapper.getServiceName().equals("*-*-*"));

    TradingRelationship serviceId = new TradingRelationship("src", "dest",
        "type");
    mapper.setServiceName(null);
    mapper.setTradingRelationship(serviceId);
    assertTrue(mapper.getServiceName().equals("src-dest-type"));
  }

  public void testRetrieveDefaultNameFromEmptyServiceName() {
    ServiceNameMapper mapper = new ServiceNameMapper();
    assertTrue(mapper.getServiceName().equals("*-*-*"));

    TradingRelationship serviceId = new TradingRelationship("src", "dest", "type");
    mapper.setServiceName("");
    mapper.setTradingRelationship(serviceId);
    assertTrue(mapper.getServiceName().equals("src-dest-type"));
  }

  public void testRetrieveConfiguredName() {
    ServiceNameMapper mapper = new ServiceNameMapper();

    TradingRelationship serviceId = new TradingRelationship("src", "dest",
        "type");

    mapper.setTradingRelationship(serviceId);
    mapper.setServiceName("over-ride");
    assertTrue(mapper.getServiceName().equals("over-ride"));
  }

  public void testBug886() throws Exception {
    AdaptrisMarshaller cm = DefaultMarshaller.getDefaultMarshaller();
    ServiceNameMapper m = new ServiceNameMapper("src", "dst", "type",
        "logicalName");
    String xml = cm.marshal(m);
    ServiceNameMapper result = (ServiceNameMapper) cm.unmarshal(xml);
    assertEquals("service name", m.getServiceName(), result.getServiceName());
  }

}
