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

package com.adaptris.core.services;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Replaces the payload with something built from a template and optional metadata keys.
 * <p>
 * Takes the template from the payload and replaces parts of the template
 * either by resolving the {@code %message} expression language or with values from various metadata
 * keys specified in {@link #setMetadataTokens(KeyValuePairSet)} to create a new payload.
 * </p>
 * <p>
 * Since under the covers it uses the JDK regular expression engine, take care when your replacement
 * may contain special regular expression characters (such as {@code \} and {@code $}
 * </p>
 *
 * @config payload-from-payload-template
 *
 */
@XStreamAlias("payload-from-payload")
@AdapterComponent
@ComponentProfile(summary = "Construct a new payload based on metadata and a template taken from the current payload", tag = "payload,service,template", since = "4.2.0")
@DisplayOrder(order = {"metadataTokens", "multiLineExpression", "quoteReplacement", "quiet"})
public class PayloadFromPayloadService extends ServiceImp {

  /**
   * The metadata tokens that will be used to perform metadata substitution.
   * <p>
   * For the purposes of this service, the key to the key-value-pair is the
   * metadata key, and the value is the token that will be replaced within the
   * template
   * </p>
   */
  @NotNull
  @AutoPopulated
  @Valid
  @Getter
  @Setter
  private KeyValuePairSet metadataTokens;

  /**
   * If any metadata value contains special characters then ensure that they are escaped.
   * <p>
   * Set this flag to make sure that special characters are treated literally by the regular expression engine.
   * <p>
   */
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "true")
  @Getter
  @Setter
  private Boolean quoteReplacement;

  /**
   * Normally this service logs everything that is being replaced with can lead to excessive logging.
   *
   * @param quiet true or false, default false if not specified.
   */
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean quiet;

  /**
   * Whether or not to handle expressions using {@code Pattern#DOTALL} mode for matching.
   *
   * <p>
   * The value here is passed to {@link InterlokMessage#resolve(String, boolean)}. True will allow you to do replacements on
   * multi-line templates; It defaults to true which means that multi-line templates along the lines of will be supported.
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
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  @Getter
  @Setter
  private Boolean multiLineExpression;

  public PayloadFromPayloadService() {
    setMetadataTokens(new KeyValuePairSet());
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    String payload = msg.resolve(StringUtils.defaultIfEmpty(msg.getContent(), ""), multiLineExpression());
    replacePayloadUsingTemplate(msg, payload);
  }

  protected void replacePayloadUsingTemplate(AdaptrisMessage message, String payloadTemplate) {
    for (KeyValuePair kvp : getMetadataTokens().getKeyValuePairs()) {
      if (message.getMetadataValue(kvp.getKey()) != null) {
        if (!quiet()) {
          log.trace("Replacing {} with {}", kvp.getValue(), message.getMetadataValue(kvp.getKey()));
        }
        payloadTemplate = payloadTemplate.replaceAll(kvp.getValue(), munge(message.getMetadataValue(kvp.getKey())));
      }
      else {
        if (!quiet()) {
          log.trace("{} returns no value; no substitution", kvp.getKey());
        }
      }
    }
    message.setContent(payloadTemplate, message.getContentEncoding());
    return;
  }

  protected String munge(String s) {
    return quoteReplacement() ? Matcher.quoteReplacement(s) : s;
  }

  @Override
  protected void initService() throws CoreException {}

  @Override
  protected void closeService() {}

  @Override
  public void prepare() throws CoreException {}

  public <T extends PayloadFromPayloadService> T withMetadataTokens(KeyValuePairSet tokens) {
    setMetadataTokens(tokens);
    return (T) this;
  }

  public <T extends PayloadFromPayloadService> T withMetadataTokens(Map<String, String> tokens) {
    return withMetadataTokens(new KeyValuePairSet(tokens));
  }

  public <T extends PayloadFromPayloadService> T withQuoteReplacement(Boolean b) {
    setQuoteReplacement(b);
    return (T) this;
  }

  protected boolean quoteReplacement() {
    return BooleanUtils.toBooleanDefaultIfNull(getQuoteReplacement(), true);
  }

  public <T extends PayloadFromPayloadService> T withQuietMode(Boolean quiet) {
    setQuiet(quiet);
    return (T) this;
  }

  protected boolean quiet() {
    return BooleanUtils.toBooleanDefaultIfNull(getQuiet(), false);
  }

  public <T extends PayloadFromPayloadService> T withMultiLineExpression(Boolean b) {
    setMultiLineExpression(b);
    return (T) this;
  }

  protected boolean multiLineExpression() {
    return BooleanUtils.toBooleanDefaultIfNull(getMultiLineExpression(), true);
  }
}
