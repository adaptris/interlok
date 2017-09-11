package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.util.Args;

/**
 * <p>
 * Contains behaviour common to the <code>MetadataConverters</code>.
 * </p>
 *
 * @author mwarman
 */
public abstract class MetadataConverter {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  @Valid
  @NotNull
  @AutoPopulated
  private MetadataFilter metadataFilter;

  @InputFieldDefault(value = "false")
  @AdvancedConfig
  private Boolean strictConversion;

  /**
   * <p>
   * Creates a new instance. Default metadata filter is <code>NoOpMetadataFilter</code>.
   * </p>
   */
  public MetadataConverter() {
    setMetadataFilter(new NoOpMetadataFilter());
  }

  public MetadataConverter(MetadataFilter metadataFilter) {
    this();
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

  /**
   * <code>MetadataElement</code> key and value set as property of <code>javax.jms.Message</code> using
   * <code>setStringProperty(String key, String value)</code>.
   *
   * @param element the <code>MetadataElement</code> to use.
   * @param out the <code>javax.jms.Message</code> to set the property on.
   * @throws JMSException
   */
  public void setProperty(MetadataElement element, Message out) throws JMSException {
    log.trace("Setting JMS Metadata " + element + " as string");
    out.setStringProperty(element.getKey(), element.getValue());
  }

  /**
   * <code>MetadataFilter</code> applied to <code>MetadataCollection</code>
   * @return The set <code>MetadataFilter</code>
   */
  public MetadataFilter getMetadataFilter() {
    return metadataFilter;
  }

  public void setMetadataFilter(MetadataFilter metadataFilter) {
    this.metadataFilter = Args.notNull(metadataFilter, "metadataFilter");
  }

  public Boolean getStrictConversion() {
    return strictConversion;
  }

  /**
   * Specify whether or not conversions should be strict.
   * <p>
   * If conversion to the right type cannot happen (e.g. it's not an Integer), then we throw a JMS Exception.
   * </p>
   * 
   * @param b true to enforce strictness, default is null (false).
   */
  public void setStrictConversion(Boolean b) {
    this.strictConversion = b;
  }

  protected boolean strict() {
    return getStrictConversion() != null ? getStrictConversion().booleanValue() : false;
  }

}
