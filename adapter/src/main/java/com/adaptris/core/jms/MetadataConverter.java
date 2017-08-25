package com.adaptris.core.jms;

import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.NoOpMetadataFilter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.validation.Valid;

/**
 * <p>
 * Contains behaviour common to the <code>MetadataConverters</code>.
 * </p>
 *
 * @author mwarman
 */
public abstract class MetadataConverter {

  @Valid
  private MetadataFilter metadataFilter;

  /**
   * <p>
   * Creates a new instance. Default metadata filter is <code>NoOpMetadataFilter</code>.
   * </p>
   */
  public MetadataConverter() {
    setMetadataFilter(new NoOpMetadataFilter());
  }

  public MetadataConverter(MetadataFilter metadataFilter) {
    setMetadataFilter(metadataFilter);
  }

  /**
   * <code>MetadataCollection</code> filtered using {@link #getMetadataFilter()} and iterated passing
   * <code>MetadataElement</code> and <code>javax.jms.Message</code> on to
   * {@link #setProperty(MetadataElement element, Message out) setProperty}.
   *
   * @param metadataCollection the <code>MetadataCollection</code> to use.
   * @param out the <code>javax.jms.Message</code> to set the properties on.
   * @throws JMSException
   */
  public void moveMetadata(MetadataCollection metadataCollection, Message out) throws JMSException {
    for (MetadataElement element : getMetadataFilter().filter(metadataCollection)) {
      if (!MetadataHandler.isReserved(element.getKey())) {
        setProperty(element, out);
      }
    }
  }

  public abstract void setProperty(MetadataElement element, Message out) throws JMSException;

  /**
   * <code>MetadataFilter</code> applied to <code>MetadataCollection</code>
   * @return The set <code>MetadataFilter</code>
   */
  public MetadataFilter getMetadataFilter() {
    return metadataFilter;
  }

  public void setMetadataFilter(MetadataFilter metadataFilter) {
    this.metadataFilter = metadataFilter;
  }
}
