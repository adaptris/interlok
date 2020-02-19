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

import java.util.Map;
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
import com.adaptris.interlok.util.Args;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Replaces the payload with something built from a template and optional metadata keys.
 * <p>
 * Takes the template stored in {@link #setTemplate(String)} and replaces parts of the template either by resolving the
 * {@code %message} expression language or with values from various metadata keys specified in
 * {@link #setMetadataTokens(KeyValuePairSet)} to create a new payload. This replaces {@link PayloadFromMetadataService}.
 * </p>
 * <p>
 * Since under the covers it uses the JDK regular expression engine, take care when your replacement may contain special regular
 * expression characters (such as {@code \} and {@code $}
 * </p>
 * 
 * @config payload-from-template
 * 
 */
@XStreamAlias("payload-from-template")
@AdapterComponent
@ComponentProfile(summary = "Construct a new payload based on metadata and a template", tag = "service,metadata", since = "3.10.0")
@DisplayOrder(order = {"template", "metadataTokens", "multiLineExpression", "quoteReplacement", "quiet"})
public class PayloadFromTemplateService extends ServiceImp {
  
  @NotNull
  @AutoPopulated
  @Valid
  private KeyValuePairSet metadataTokens;
  @MarshallingCDATA
  @InputFieldDefault(value = "")
  @InputFieldHint(expression = true, style="BLANKABLE")
  private String template = null;
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "true")
  private Boolean quoteReplacement;
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "false")
  private Boolean quiet;
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean multiLineExpression;

  public PayloadFromTemplateService() {
    setMetadataTokens(new KeyValuePairSet());
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    String payload = msg.resolve(StringUtils.defaultIfEmpty(template, ""), multiLineExpression());
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
    return quoteReplacement() ? Matcher.quoteReplacement(s) : s;
  }

  @Override
  protected void initService() throws CoreException {}

  @Override
  protected void closeService() {}

  @Override
  public void prepare() throws CoreException {}

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
    this.metadataTokens = Args.notNull(metadataTokens, "metadata-tokens");
  }

  public <T extends PayloadFromTemplateService> T withMetadataTokens(KeyValuePairSet tokens) {
    setMetadataTokens(tokens);
    return (T) this;
  }

  public <T extends PayloadFromTemplateService> T withMetadataTokens(Map<String, String> tokens) {
    return withMetadataTokens(new KeyValuePairSet(tokens));
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

  public <T extends PayloadFromTemplateService> T withTemplate(String b) {
    setTemplate(b);
    return (T) this;
  }


  public Boolean getQuoteReplacement() {
    return quoteReplacement;
  }

  /**
   * If any metadata value contains special characters then ensure that they are escaped.
   * <p>
   * Set this flag to make sure that special characters are treated literally by the regular expression engine.
   * <p>
   *
   * @see Matcher#quoteReplacement(String)
   * @param b the value to set, defaults to true if not explicitly configured.
   */
  public void setQuoteReplacement(Boolean b) {
    quoteReplacement = b;
  }
  
  public <T extends PayloadFromTemplateService> T withQuoteReplacement(Boolean b) {
    setQuoteReplacement(b); 
    return (T) this;
  }

  protected boolean quoteReplacement() {
    return BooleanUtils.toBooleanDefaultIfNull(getQuoteReplacement(), true);
  }

  public Boolean getQuiet() {
    return quiet;
  }

  /**
   * Normally this service logs everything that is being replaced with can lead to excessive logging.
   * 
   * @param quiet true or false, default false if not specified.
   */
  public void setQuiet(Boolean quiet) {
    this.quiet = quiet;
  }

  public <T extends PayloadFromTemplateService> T withQuietMode(Boolean quiet) {
    setQuiet(quiet);
    return (T) this;
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
   * multi-line templates; It defaults to false. Setting it to true means that multi-line templates along the lines of will be
   * supported.
   * 
   * <pre>
   * {@code 
   * { 
   *   "key": "%message{metadataKey}", 
   *   "key2: "%message{anotherMetadatKey}", 
   * } 
   * }
   * </pre>
   * </p>
   * 
   * @param b true/false, default is true if not specified.
   */
  public void setMultiLineExpression(Boolean b) {
    this.multiLineExpression = b;
  }
  
  public <T extends PayloadFromTemplateService> T withMultiLineExpression(Boolean b) {
    setMultiLineExpression(b); 
    return (T) this;
  }
  
  protected boolean multiLineExpression() {
    return BooleanUtils.toBooleanDefaultIfNull(getMultiLineExpression(), true);
  }
}
