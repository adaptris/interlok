/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.services.metadata;

import java.util.Iterator;
import java.util.regex.Matcher;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.MarshallingCDATA;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
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
 * 
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("payload-from-metadata-service")
@AdapterComponent
@ComponentProfile(summary = "Construct a new payload based on metadata and a template", tag = "service,metadata")
@DisplayOrder(order = {"template", "metadataTokens", "escapeBackslash"})
public class PayloadFromMetadataService extends ServiceImp {

  private static final String DEFAULT_ENCODING = "UTF-8";
  
  @NotNull
  @AutoPopulated
  private KeyValuePairSet metadataTokens;
  @MarshallingCDATA
  private String template = null;
  @InputFieldDefault(value = "true")
  private Boolean escapeBackslash;

  public PayloadFromMetadataService() {
    setMetadataTokens(new KeyValuePairSet());
  }

  public PayloadFromMetadataService(String template) {
    this();
    setTemplate(template);
  }

  /**
   * @see com.adaptris.core.Service#doService(com.adaptris.core.AdaptrisMessage)
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
    msg.setContent(payload, msg.getContentEncoding());
  }
  

  private String munge(String s) {
    String result = s;
    if (escapeBackslash()) {
//      result = s.replaceAll("\\\\", "\\\\\\\\");
      result = Matcher.quoteReplacement(s);
    }
    return result;
  }

  @Override
  protected void initService() throws CoreException {
    if (getTemplate() == null) {
      throw new CoreException("Template is null");
    }
  }

  @Override
  protected void closeService() {

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
  public void prepare() throws CoreException {
  }


}
