package com.adaptris.core.jms;

import com.adaptris.core.BaseCase;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.metadata.RegexMetadataFilter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * @author mwarman
 */
public class StringMetadataConverterTest extends MetadataConverterCase {

  private static final String VALUE = "value";

  public StringMetadataConverterTest(String name) {
    super(name);
  }

  @Override
  MetadataConverter createConverter() {
    return new StringMetadataConverter();
  }

  @Override
  String getStringValue() {
    return VALUE;
  }

  @Override
  void assertValue(Message jmsMsg) throws JMSException{
    assertEquals(VALUE, jmsMsg.getStringProperty(HEADER));
  }

  public void testConstruct() throws Exception {
    StringMetadataConverter mc = new StringMetadataConverter();
    assertTrue(mc.getMetadataFilter() instanceof NoOpMetadataFilter);
    mc = new StringMetadataConverter(new RegexMetadataFilter());
    assertTrue(mc.getMetadataFilter() instanceof RegexMetadataFilter);
  }


}
