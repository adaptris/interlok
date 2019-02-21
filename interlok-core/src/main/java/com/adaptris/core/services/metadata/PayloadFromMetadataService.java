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

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

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
import com.adaptris.interlok.types.InterlokMessage;
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
@DisplayOrder(order = {"template", "metadataTokens", "dotAll", "escapeBackslash", "quiet"})
public class PayloadFromMetadataService extends ServiceImp {
  
  @NotNull
  @AutoPopulated
  @Valid
  private KeyValuePairSet metadataTokens;
  @MarshallingCDATA
  @InputFieldDefault(value = "")
  @InputFieldHint(expression = true, style="BLANKABLE")
  private String template = null;
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean escapeBackslash;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean quiet;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean multiLineExpression;

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
    String payload = msg.resolve(StringUtils.defaultIfEmpty(template, ""), dotAll());
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
   * Set the metadata tokens that will be used to perform metadata substitution.
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
  
  public PayloadFromMetadataService withEscapeBackslash(Boolean b) {
    setEscapeBackslash(b); 
    return this;
  }

  private boolean escapeBackslash() {
    return BooleanUtils.toBooleanDefaultIfNull(getEscapeBackslash(), true);
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

  public PayloadFromMetadataService withQuietMode(Boolean quiet) {
    setQuiet(quiet);
    return this;
  }
  
  private boolean quiet() {
    return BooleanUtils.toBooleanDefaultIfNull(getQuiet(), false);
  }

  public Boolean getMultiLineExpression() {
    return multiLineExpression;
  }

  /**
   * Whether or not to handle expressions using {@code Pattern#DOTALL} mode for matching.
   * 
   * <p>
   * The value here is passed to {@link InterlokMessage#resolve(String, boolean)}. True will allow you to do replacements on
   * multi-line templates; for backwards compatiblity reasons, it defaults to false. Setting it to true means that multi-line 
   * templates along the lines of will be supported.
   * <pre>
   * {@code {
   *   "key": "%message{metadataKey}",
   *   "key2: "%message{anotherMetadatKey}",
   * }
   * }
   * </p>
   * 
   * @param b true, default is false if not specified.
   * @since 3.8.3
   */
  public void setMultiLineExpression(Boolean b) {
    this.multiLineExpression = b;
  }
  
  public PayloadFromMetadataService withMultiLineExpression(Boolean b) {
    setMultiLineExpression(b); 
    return this;
  }
  
  private boolean dotAll() {
    return BooleanUtils.toBooleanDefaultIfNull(getMultiLineExpression(), false);
  }
  
  @Override
  public void prepare() throws CoreException {
  }

}
