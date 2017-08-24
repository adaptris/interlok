package com.adaptris.core.jms;

import com.adaptris.core.MetadataCollection;
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
@XStreamAlias("metadata-converter")
public abstract class MetadataConverter {

  private MetadataFilter metadataFilter;

  public MetadataConverter() {
    setMetadataFilter(new NoOpMetadataFilter());
  }

  public MetadataConverter(MetadataFilter metadataFilter) {
    setMetadataFilter(metadataFilter);
  }

  public void moveMetadata(MetadataCollection metadataCollection, Message out) throws JMSException {
    for (MetadataElement element : getMetadataFilter().filter(metadataCollection)) {
      if (!MetadataHandler.isReserved(element.getKey())) {
        setProperty(out, element);
      }
    }
  }

  public abstract void setProperty(Message out, MetadataElement element) throws JMSException;

  public MetadataFilter getMetadataFilter() {
    return metadataFilter;
  }

  public void setMetadataFilter(MetadataFilter metadataFilter) {
    this.metadataFilter = metadataFilter;
  }
}
