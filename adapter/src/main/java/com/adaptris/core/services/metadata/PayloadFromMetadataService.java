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

import java.util.regex.Matcher;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
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
  
  @NotNull
  @AutoPopulated
  @Valid
  private KeyValuePairSet metadataTokens;
  @MarshallingCDATA
  @InputFieldDefault(value = "")
  @InputFieldHint(expression = true)
  private String template = null;
  @InputFieldDefault(value = "true")
  private Boolean escapeBackslash;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean quiet;

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
    String payload = msg.resolve(StringUtils.defaultIfEmpty(template, ""));
    for (KeyValuePair kvp : getMetadataTokens().getKeyValuePairs()) {
      if (msg.getMetadataValue(kvp.getKey()) != null) {
        if (!quiet()) {
          log.trace("Replacing {} with {}", kvp.getValue(), msg.getMetadataValue(kvp.getKey()));
        }
        payload = payload.replaceAll(kvp.getValue(), munge(msg.getMetadataValue(kvp.getKey())));
      }
      else {
        if (!quiet()) {
          log.trace("{} returns no value; no substitution", kvp.getKey());
        }
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
   * Set the template document that will be used as the template for generating a new document.
   *
   * @param s the template to set (supports metadata expansion via {@code %message{key}}).
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

  public Boolean getQuiet() {
    return quiet;
  }

  /**
   * Normally this service logs everything that is being replaced; set to true to stop it.
   * 
   * @param quiet true or false, default false if not specified.
   */
  public void setQuiet(Boolean quiet) {
    this.quiet = quiet;
  }

  private boolean quiet() {
    return getQuiet() != null ? getQuiet().booleanValue() : false;
  }

  @Override
  public void prepare() throws CoreException {
  }



}
