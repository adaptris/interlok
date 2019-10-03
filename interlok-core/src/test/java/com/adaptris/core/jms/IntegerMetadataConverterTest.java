package com.adaptris.core.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import javax.jms.JMSException;
import javax.jms.Message;
import org.junit.Test;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.metadata.RegexMetadataFilter;

/**
 * @author mwarman
 */
public class IntegerMetadataConverterTest extends ConvertingMetadataConverterCase {


  private static final int VALUE = 10;
  private static final String STRING_VALUE = String.valueOf(VALUE);

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

  @Test
  public void testConstruct() throws Exception {
    IntegerMetadataConverter mc = new IntegerMetadataConverter();
    assertTrue(mc.getMetadataFilter() instanceof NoOpMetadataFilter);
    mc = new IntegerMetadataConverter(new RegexMetadataFilter());
    assertTrue(mc.getMetadataFilter() instanceof RegexMetadataFilter);
  }

}
