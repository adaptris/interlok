/*
 * $RCSfile: PayloadFromMetadataService.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/04/06 12:22:15 $
 * $Author: lchan $
 */
package com.adaptris.core.services.metadata;

import java.util.Iterator;
import java.util.regex.Matcher;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.MarshallingCDATA;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Replaces the payload with something built from metadata.
 * <p>
 * Takes the template stored in {@link #setTemplate(String)} and replaces parts of the template with values from various metadata
 * keys specified in {@link #setMetadataTokens(KeyValuePairSet)} to create a new payload.
 * </p>
 * <p>
 * Uses the JDK regular expression engine, take care when replacing special regular expression values.
 * </p>
 * 
 * @config payload-from-metadata-service
 * 
 * @license BASIC
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("payload-from-metadata-service")
public class PayloadFromMetadataService extends ServiceImp {

  private static final String DEFAULT_ENCODING = "UTF-8";
  
  @NotNull
  @AutoPopulated
  private KeyValuePairSet metadataTokens;
  @MarshallingCDATA
  private String template = null;
  private Boolean escapeBackslash;

  public PayloadFromMetadataService() {
    setMetadataTokens(new KeyValuePairSet());
  }

  public PayloadFromMetadataService(String template) {
    this();
    setTemplate(template);
  }

  /**
   * @see Service#doService(com.adaptris.core.AdaptrisMessage)
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    String payload = template;
    for (Iterator i = metadataTokens.getKeyValuePairs().iterator(); i.hasNext();) {
      KeyValuePair kvp = (KeyValuePair) i.next();
      if (msg.getMetadataValue(kvp.getKey()) != null) {
        log.trace("Replacing " + kvp.getValue() + " with "
            + msg.getMetadataValue(kvp.getKey()));
        payload = payload.replaceAll(kvp.getValue(), munge(msg
            .getMetadataValue(kvp.getKey())));
      }
      else {
        log.trace(kvp.getKey() + " returns no value; no substitution");
      }
    }
    msg.setStringPayload(payload, msg.getCharEncoding());
  }
  

  private String munge(String s) {
    String result = s;
    if (escapeBackslash()) {
//      result = s.replaceAll("\\\\", "\\\\\\\\");
      result = Matcher.quoteReplacement(s);
    }
    return result;
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#close()
   */
  public void close() {
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  public void init() throws CoreException {
    if (getTemplate() == null) {
      throw new CoreException("Template is null");
    }
  }

  /**
   * @return the metadataTokens
   */
  public KeyValuePairSet getMetadataTokens() {
    return metadataTokens;
  }

  /**
   * Set the metadata tokens that will form the XML.
   * <p>
   * For the purposes of this service, the key to the key-value-pair is the
   * metadata key, and the value is the token that will be replaced within the
   * template
   * </p>
   *
   * @param metadataTokens the metadataTokens to set
   */
  public void setMetadataTokens(KeyValuePairSet metadataTokens) {
    this.metadataTokens = metadataTokens;
  }

  /**
   * @return the template
   */
  public String getTemplate() {
    return template;
  }

  /**
   * Set the template document that will be used as the template for generating
   * a new document.
   *
   * @param s the template to set
   */
  public void setTemplate(String s) {
    template = s;
  }


  public Boolean getEscapeBackslash() {
    return escapeBackslash;
  }

  /**
   * If any metadata value contains backslashes then ensure that they are escaped.
   * <p>
   * Set this flag to make sure that special characters are treated literally by the regular expression engine.
   * <p>
   *
   * @see Matcher#quoteReplacement(String)
   * @param b the value to set
   */
  public void setEscapeBackslash(Boolean b) {
    escapeBackslash = b;
  }

  boolean escapeBackslash() {
    return getEscapeBackslash() != null ? getEscapeBackslash().booleanValue() : true;
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }

}
