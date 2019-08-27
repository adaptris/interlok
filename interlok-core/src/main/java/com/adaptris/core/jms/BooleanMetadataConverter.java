package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.metadata.MetadataFilter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 *
 * <code>MetadataElement</code> key and value set as property of <code>javax.jms.Message</code>
 * using <code>setBooleanProperty(String key, boolean value)</code>.
 *
 * @config jms-boolean-metadata-converter
 * @author mwarman
 */
@XStreamAlias("jms-boolean-metadata-converter")
@DisplayOrder(order = {"metadataFilter"})
public class BooleanMetadataConverter extends MetadataConverter {

  /** @see MetadataConverter#MetadataConverter() */
  public BooleanMetadataConverter() {
    super();
  }

  public BooleanMetadataConverter(MetadataFilter metadataFilter) {
    super(metadataFilter);
  }

  /**
   * <code>MetadataElement</code> key and value set as property of <code>javax.jms.Message</code>
   * using <code>setBooleanProperty(String key, boolean value)</code>.
   *
   * @param element the <code>MetadataElement</code> to use.
   * @param out the <code>javax.jms.Message</code> to set the property on.
   * @throws JMSException
   */
  @Override
  public void setProperty(MetadataElement element, Message out) throws JMSException {
    try {
      Boolean value = convert(element.getValue());
      log.trace("Setting JMS Metadata {} as boolean", element);
      out.setBooleanProperty(element.getKey(), value.booleanValue());
    } catch (NotBooleanException e) {
      if (strict()) {
        throw JmsUtils.wrapJMSException(e);
      }
      super.setProperty(element, out);
    }
  }

  private Boolean convert(String value) throws NotBooleanException {
    Boolean result = BooleanUtils.toBooleanObject(value);
    if (result == null) {
      throw new NotBooleanException(value + " is not boolean");
    }
    return result;
  }

  private class NotBooleanException extends Exception {

    private static final long serialVersionUID = 2443244004703212227L;

    public NotBooleanException(String msg) {
      super(msg);
    }
  }
}
