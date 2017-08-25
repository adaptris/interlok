package com.adaptris.core.jms;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.metadata.MetadataFilter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * <code>MetadataElement</code> key and value set as property of <code>javax.jms.Message</code>
 * using <code>setStringProperty(String key, String value)</code>.
 *
 * @config jms-string-metadata-converter
 * @author mwarman
 */
@XStreamAlias("jms-string-metadata-converter")
@DisplayOrder(order = {"metadataFilter"})
public class StringMetadataConverter extends MetadataConverter {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  /** @see MetadataConverter#MetadataConverter() */
  public StringMetadataConverter() {
    super();
  }

  public StringMetadataConverter(MetadataFilter metadataFilter) {
    super(metadataFilter);
  }

  /**
   * <code>MetadataElement</code> key and value set as property of <code>javax.jms.Message</code>
   * using <code>setStringProperty(String key, String value)</code>.
   *
   * @param element the <code>MetadataElement</code> to use.
   * @param out the <code>javax.jms.Message</code> to set the property on.
   * @throws JMSException
   */
  @Override
  public void setProperty(MetadataElement element, Message out) throws JMSException {
    log.trace("Setting JMS Metadata " + element + " as string");
    out.setStringProperty(element.getKey(),element.getValue());
  }
}
