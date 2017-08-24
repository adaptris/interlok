package com.adaptris.core.jms;

import com.adaptris.core.MetadataElement;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author mwarman
 */
@XStreamAlias("jms-string-metadata-converter")
public class StringMetadataConverter extends MetadataConverter {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  public StringMetadataConverter() {
    super();
  }

  public StringMetadataConverter(MetadataFilter metadataFilter) {
    super(metadataFilter);
  }

  @Override
  public void setProperty(MetadataElement element, Message out) throws JMSException {
    log.trace("Setting JMS Metadata " + element + " as string");
    out.setStringProperty(element.getKey(),element.getValue());
  }
}
