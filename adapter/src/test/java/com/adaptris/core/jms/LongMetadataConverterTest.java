package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.Message;

import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.metadata.RegexMetadataFilter;

/**
 * @author mwarman
 */
public class LongMetadataConverterTest extends ConvertingMetadataConverterCase {

  private static final long VALUE = 10L;
  private static final String STRING_VALUE = String.valueOf(VALUE);

  public LongMetadataConverterTest(String name) {
    super(name);
  }

  @Override
  MetadataConverter createConverter() {
    return new LongMetadataConverter();
  }

  @Override
  String getStringValue() {
    return STRING_VALUE;
  }

  @Override
  void assertValue(Message jmsMsg) throws JMSException {
    assertEquals(VALUE, jmsMsg.getLongProperty(HEADER));
  }

  public void testConstruct() throws Exception {
    LongMetadataConverter mc = new LongMetadataConverter();
    assertTrue(mc.getMetadataFilter() instanceof NoOpMetadataFilter);
    mc = new LongMetadataConverter(new RegexMetadataFilter());
    assertTrue(mc.getMetadataFilter() instanceof RegexMetadataFilter);
  }


}
