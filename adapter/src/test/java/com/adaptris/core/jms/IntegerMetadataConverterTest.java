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
public class IntegerMetadataConverterTest extends MetadataConverterCase {


  private static final int VALUE = 10;
  private static final String STRING_VALUE = String.valueOf(VALUE);

  public IntegerMetadataConverterTest(String name) {
    super(name);
  }

  @Override
  MetadataConverter createConverter() {
    return new IntegerMetadataConverter();
  }

  @Override
  String getStringValue() {
    return STRING_VALUE;
  }

  @Override
  void assertValue(Message jmsMsg) throws JMSException {
    assertEquals(VALUE, jmsMsg.getIntProperty(HEADER));
  }

  public void testConstruct() throws Exception {
    IntegerMetadataConverter mc = new IntegerMetadataConverter();
    assertTrue(mc.getMetadataFilter() instanceof NoOpMetadataFilter);
    mc = new IntegerMetadataConverter(new RegexMetadataFilter());
    assertTrue(mc.getMetadataFilter() instanceof RegexMetadataFilter);
  }

}
