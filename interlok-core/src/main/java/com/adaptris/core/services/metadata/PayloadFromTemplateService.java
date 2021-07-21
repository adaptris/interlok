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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.annotation.MarshallingCDATA;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.PayloadFromPayloadService;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Replaces the payload with something built from a template and optional metadata keys.
 * <p>
 * Takes the template stored in {@link #setTemplate(String)} and replaces parts of the template
 * either by resolving the {@code %message} expression language or with values from various metadata
 * keys specified in {@link #setMetadataTokens(KeyValuePairSet)} to create a new payload.
 * </p>
 * <p>
 * Since under the covers it uses the JDK regular expression engine, take care when your replacement
 * may contain special regular expression characters (such as {@code \} and {@code $}
 * </p>
 *
 * @config payload-from-template
 *
 */
@XStreamAlias("payload-from-template")
@AdapterComponent
@ComponentProfile(summary = "Construct a new payload based on metadata and a template", tag = "service,metadata", since = "3.10.0")
@DisplayOrder(order = {"template", "metadataTokens", "multiLineExpression", "quoteReplacement", "quiet"})
public class PayloadFromTemplateService extends PayloadFromPayloadService {

  @MarshallingCDATA
  @InputFieldDefault(value = "")
  @InputFieldHint(expression = true, style="BLANKABLE")
  @Getter
  @Setter
  private String template = null;

  public PayloadFromTemplateService() {
    super();
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    String payload = msg.resolve(StringUtils.defaultIfEmpty(template, ""), multiLineExpression());
    replacePayloadUsingTemplate(msg, payload);
  }

  public <T extends PayloadFromTemplateService> T withTemplate(String b) {
    setTemplate(b);
    return (T) this;
  }
}
