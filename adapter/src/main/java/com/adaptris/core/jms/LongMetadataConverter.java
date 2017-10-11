package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.lang.math.NumberUtils;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.metadata.MetadataFilter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <code>MetadataElement</code> key and value set as property of <code>javax.jms.Message</code>
 * using <code>setLongProperty(String key, long value)</code>.
 *
 * @config jms-long-metadata-converter
 * @author mwarman
 */
@XStreamAlias("jms-long-metadata-converter")
@DisplayOrder(order = {"metadataFilter"})
public class LongMetadataConverter extends MetadataConverter {

  /** @see MetadataConverter#MetadataConverter() */
  public LongMetadataConverter() {
    super();
  }

  public LongMetadataConverter(MetadataFilter metadataFilter) {
    super(metadataFilter);
  }

  /**
   * <code>MetadataElement</code> key and value set as property of <code>javax.jms.Message</code>
   * using <code>setLongProperty(String key, long value)</code>.
   *
   * @param element the <code>MetadataElement</code> to use.
   * @param out the <code>javax.jms.Message</code> to set the property on.
   * @throws JMSException
   */
  @Override
  public void setProperty(MetadataElement element, Message out) throws JMSException {
    try {
      Long value = NumberUtils.createLong(element.getValue());
      log.trace("Setting JMS Metadata {} as a long", element);
      out.setLongProperty(element.getKey(), value.longValue());
    }
    catch (NumberFormatException | NullPointerException e) {
      if (strict()) {
        throw JmsUtils.wrapJMSException(e);
      }
      super.setProperty(element, out);
    }
  }
}
