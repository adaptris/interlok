package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.Message;

import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.metadata.RegexMetadataFilter;

/**
 * @author mwarman
 */
public class BooleanMetadataConverterTest extends ConvertingMetadataConverterCase {

  private static final boolean VALUE = true;
  private static final String STRING_VALUE = String.valueOf(VALUE);

  public BooleanMetadataConverterTest(String name) {
    super(name);
  }

  @Override
  MetadataConverter createConverter() {
    return new BooleanMetadataConverter();
  }

  @Override
  String getStringValue() {
    return STRING_VALUE;
  }

  @Override
  void assertValue(Message jmsMsg) throws JMSException {
    assertEquals(VALUE, jmsMsg.getBooleanProperty(HEADER));
  }

  public void testConstruct() throws Exception {
    BooleanMetadataConverter mc = new BooleanMetadataConverter();
    assertTrue(mc.getMetadataFilter() instanceof NoOpMetadataFilter);
    mc = new BooleanMetadataConverter(new RegexMetadataFilter());
    assertTrue(mc.getMetadataFilter() instanceof RegexMetadataFilter);
  }

}
