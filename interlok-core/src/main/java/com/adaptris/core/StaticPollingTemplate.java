/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core;

import org.apache.commons.lang3.StringUtils;
import com.adaptris.annotation.MarshallingCDATA;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Provides a static template for {@link PollingTrigger}.
 * 
 * @config static-polling-trigger-template
 */
@XStreamAlias("static-polling-trigger-template")
public class StaticPollingTemplate implements PollingTrigger.MessageProvider {

  @MarshallingCDATA
  private String template;

  public StaticPollingTemplate() {

  }

  public StaticPollingTemplate(String s) {
    this();
    setTemplate(s);
  }

  /**
   *
   * @param s the template message to use
   */
  public void setTemplate(String s) {
    template = s;
  }

  /**
   *
   * @return the template message to use
   */
  public String getTemplate() {
    return template;
  }

  @Override
  public AdaptrisMessage createMessage(AdaptrisMessageFactory fac) {
    return fac.newMessage(StringUtils.defaultIfBlank(getTemplate(), ""));
  }

}
