package com.adaptris.core.services.metadata;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.util.LifecycleHelper;

public class ConvertObjectMetadataTest extends MetadataServiceExample {

  public ConvertObjectMetadataTest(String name) {
    super(name);
  }

  @Override
  public void setUp() {
  }

  public void testDoService() throws Exception {
    ConvertObjectMetadataService service = new ConvertObjectMetadataService("java.jms.Message.*");
    Object o1 = "java.jms.Message.JMSCorrelationID";
    Object o2 = new Object();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.getObjectMetadata().put(o1, o1);
    msg.getObjectMetadata().put(o2, o2);
    execute(service, msg);
    assertTrue(msg.containsKey(o1.toString()));
    assertEquals(o1.toString(), msg.getMetadataValue(o1.toString()));
  }

  public void testInit_NoRegexp() throws Exception {
    ConvertObjectMetadataService service = new ConvertObjectMetadataService();
    try {
      LifecycleHelper.init(service);
      fail();
    }
    catch (Exception expected) {

    }
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new ConvertObjectMetadataService("Object_Metadata_key_Regexp");
  }

}