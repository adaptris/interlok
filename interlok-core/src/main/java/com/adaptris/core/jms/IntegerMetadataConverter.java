package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.metadata.MetadataFilter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 *
 * <p>
 * <code>MetadataElement</code> key and value set as property of <code>javax.jms.Message</code>
 * using <code>setIntProperty(String key, int value)</code>.
 * </p>
 *
 * @config jms-integer-metadata-converter
 * @author mwarman
 */
@XStreamAlias("jms-integer-metadata-converter")
@DisplayOrder(order = {"metadataFilter"})
public class IntegerMetadataConverter extends MetadataConverter {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  /** @see MetadataConverter#MetadataConverter() */
  public IntegerMetadataConverter() {
    super();
  }

  public IntegerMetadataConverter(MetadataFilter metadataFilter) {
    super(metadataFilter);
  }

  /**
   * <code>MetadataElement</code> key and value set as property of <code>javax.jms.Message</code>
   * using <code>setIntProperty(String key, int value)</code>.
   *
   * @param element the <code>MetadataElement</code> to use.
   * @param out the <code>javax.jms.Message</code> to set the property on.
   * @throws JMSException
   */
  @Override
  public void setProperty(MetadataElement element, Message out) throws JMSException {
    try {
      Integer value = NumberUtils.createInteger(element.getValue());
      log.trace("Setting JMS Metadata {} as a int", element);
      out.setIntProperty(element.getKey(), value.intValue());
    }
    catch (NumberFormatException | NullPointerException e) {
      if (strict()) {
        throw JmsUtils.wrapJMSException(e);
      }
      super.setProperty(element, out);
    }
  }
}
