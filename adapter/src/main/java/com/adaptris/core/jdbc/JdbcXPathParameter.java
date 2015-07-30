package com.adaptris.core.jdbc;

import static org.apache.commons.lang.StringUtils.isEmpty;

import javax.validation.constraints.NotNull;
import javax.xml.namespace.NamespaceContext;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.jdbc.ParameterValueType;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.XmlUtils;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Stored Procedure parameter implementation, can be used only for IN Stored Procedure parameters.
 * <p>
 * When this implementation is used for an IN parameter, then an xpath expression will be evaluated from the {@link AdaptrisMessage}
 * payload and used as the parameter value. You will simply set xpath to a valid xpath expression to retrieve data from the
 * {@link AdaptrisMessage} payload.
 * </p>
 * <p>
 * Additionally you will set one or both of "name" and/or "order". "name" will map this parameter to a Stored Procedure parameter
 * using the Stored Procedures method signature. "order" will map this parameter according to the parameter number using the Stored
 * Procedures method signature. Note that the "order" starts from 1 and not 0, so the first parameter would be order 1. You will
 * also need to set the data type of the parameter; you may use any of the string types defined in {@link ParameterValueType}
 * </p>
 * 
 * @config jdbc-xpath-parameter
 * @author Aaron McGrath
 * 
 */
@XStreamAlias("jdbc-xpath-parameter")
public class JdbcXPathParameter extends NullableParameter {
  
  @NotNull
  @NotBlank
  private String xpath;
  private KeyValuePairSet namespaceContext;

  @Override
  public Object applyInputParam(AdaptrisMessage msg) throws JdbcParameterException {
    this.checkXPath();
    NamespaceContext ctx = SimpleNamespaceContext.create(namespaceContext, msg);
    XmlUtils xmlUtility = new XmlUtils(ctx);
    try {
      xmlUtility.setSource(msg.getInputStream());
      String textItem = new XPath(ctx).selectSingleTextItem(xmlUtility.getCurrentDoc(), xpath);
      return normalize(textItem);
    }
    catch (Exception ex) {
      throw new JdbcParameterException(ex);
    }
  }

  @Override
  public void applyOutputParam(Object dbValue, AdaptrisMessage msg) throws JdbcParameterException {
    throw new JdbcParameterException(this.getClass().getName() + " cannot be applied to Jdbc output parameters.");
  }
  
  private void checkXPath() throws JdbcParameterException {
    if(isEmpty(this.getXpath()))
      throw new JdbcParameterException("XPath has not been set for " + this.getClass().getName());
  }

  public String getXpath() {
    return xpath;
  }

  public void setXpath(String xpath) {
    this.xpath = xpath;
  }

  /**
   * @return the namespaceContext
   */
  public KeyValuePairSet getNamespaceContext() {
    return namespaceContext;
  }

  /**
   * Set the namespace context for resolving namespaces.
   * <ul>
   * <li>The key is the namespace prefix</li>
   * <li>The value is the namespace uri</li>
   * </ul>
   * 
   * @param kvps the namespace context
   * @see SimpleNamespaceContext#create(KeyValuePairSet)
   */
  public void setNamespaceContext(KeyValuePairSet kvps) {
    this.namespaceContext = kvps;
  }
}
