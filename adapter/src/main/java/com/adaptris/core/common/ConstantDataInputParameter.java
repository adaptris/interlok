package com.adaptris.core.common;

import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This {@code DataInputParameter} is used when you want to configure data directly in the Interlok configuration.
 * </p>
 * <p>
 * An example might be configuring the XPath expression directly in Interlok configuration used for the {@link
 * com.adaptris.core.services.xml.XPathService}.
 * </p>
 * 
 * @author amcgrath
 * @config constant-data-input-parameter
 * @license BASIC
 */
@XStreamAlias("constant-data-input-parameter")
public class ConstantDataInputParameter implements DataInputParameter<String> {

  private String value;
  
  public ConstantDataInputParameter() {
  }
  
  public ConstantDataInputParameter(String v) {
    this();
    setValue(v);
  }

  @Override
  public String extract(InterlokMessage m) throws InterlokException {
    return getValue();
  }



  public String getValue() {
    return value;
  }

  public void setValue(String v) {
    this.value = v;
  }


}
