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
@XStreamAlias("jms-boolean-metadata-converter")
public class BooleanMetadataConverter extends MetadataConverter {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  public BooleanMetadataConverter() {
    super();
  }

  public BooleanMetadataConverter(MetadataFilter metadataFilter) {
    super(metadataFilter);
  }

  @Override
  public void setProperty(Message out, MetadataElement element) throws JMSException {
    log.trace("Setting JMS Metadata " + element + " as boolean");
    out.setBooleanProperty(element.getKey(), Boolean.valueOf(element.getValue()));
  }
}
