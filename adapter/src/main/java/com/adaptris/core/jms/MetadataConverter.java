package com.adaptris.core.jms;

import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.NoOpMetadataFilter;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author mwarman
 */
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
        setProperty(element, out);
      }
    }
  }

  public abstract void setProperty(MetadataElement element, Message out) throws JMSException;

  public MetadataFilter getMetadataFilter() {
    return metadataFilter;
  }

  public void setMetadataFilter(MetadataFilter metadataFilter) {
    this.metadataFilter = metadataFilter;
  }
}
