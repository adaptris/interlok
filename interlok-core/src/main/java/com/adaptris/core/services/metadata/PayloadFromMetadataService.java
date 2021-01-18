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

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.Removal;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Replaces the payload with something else.
 * <p>
 * This follows the configuration for {@link PayloadFromTemplateService} with 2 exceptions
 * <ul>
 * <li>escape-backslash is still available to be configured but deprecated</li>
 * <li>multi-line-expression defaults to FALSE in the constructor for backwards compatibility reasons</li>
 * </ul>
 * </p>
 *
 * @config payload-from-metadata-service
 * @deprecated since 3.10.0 use {@link PayloadFromTemplateService} or {@link MetadataToPayloadService} instead; most of the time
 *             you're abusing it...
 */
@Deprecated
@XStreamAlias("payload-from-metadata-service")
@AdapterComponent
@ComponentProfile(summary = "Construct a new payload based on metadata and a template", tag = "service,metadata")
@DisplayOrder(order = {"template", "metadataTokens", "multiLineExpression", "quoteReplacement", "escapeBackslash", "quiet"})
@ConfigDeprecated(removalVersion = "4.0.0", message = "use payload-from-template or metadata-to-payload instead", groups = Deprecated.class)
public class PayloadFromMetadataService extends PayloadFromTemplateService {

  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "true")
  @Deprecated
  @ConfigDeprecated(removalVersion = "4.0.0", message = "use quote-replacement instead", groups = Deprecated.class)
  private Boolean escapeBackslash;

  private transient boolean warningLogged = false;

  public PayloadFromMetadataService() {
    setMultiLineExpression(Boolean.FALSE);
  }

  public PayloadFromMetadataService(String template) {
    this();
    setTemplate(template);
  }

  @Override
  public void prepare() throws CoreException {
    LoggingHelper.logWarning(warningLogged, () -> {
      warningLogged = true;
    }, "[{}] is a payload-from-metadata-service; use payload-from-template or metadata-to-payload instead.",
        LoggingHelper.friendlyName(this));
    super.prepare();
  }


  /**
   *
   * @deprecated since 3.10.0, use {@link #setQuoteReplacement(Boolean)} instead.
   */
  @Deprecated
  @ConfigDeprecated(removalVersion = "4.0.0", message = "use quote-replacement instead", groups = Deprecated.class)
  public Boolean getEscapeBackslash() {
    return escapeBackslash;
  }

  /**
   * If any metadata value contains backslashes then ensure that they are escaped.
   * <p>
   * Set this flag to make sure that special characters are treated literally by the regular expression engine.
   * <p>
   *
   * @deprecated since 3.10.0, use {@link #setQuoteReplacement(Boolean)} instead.
   * @see Matcher#quoteReplacement(String)
   * @param b the value to set
   */
  @Deprecated
  @Removal(version = "4.0.0", message = "use quote-replacement instead")
  public void setEscapeBackslash(Boolean b) {
    escapeBackslash = b;
  }

  /**
   *
   * @deprecated since 3.10.0, use {@link #setQuoteReplacement(Boolean)} instead.
   */
  @Deprecated
  @Removal(version = "4.0.0", message = "use quote-replacement instead")
  public PayloadFromMetadataService withEscapeBackslash(Boolean b) {
    setEscapeBackslash(b);
    return this;
  }

  @Override
  protected boolean quoteReplacement() {
    if (getEscapeBackslash() != null) {
      return BooleanUtils.toBooleanDefaultIfNull(getEscapeBackslash(), true);
    }
    return super.quoteReplacement();
  }

}
