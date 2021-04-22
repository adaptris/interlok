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

package com.adaptris.core.services.metadata.xpath;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Abstract base class for {@linkplain XpathQuery} implementations that are statically configured.
 *
 * @author lchan
 *
 */
public abstract class ConfiguredXpathQueryImpl extends XpathQueryImpl {

  @Getter
  @NotBlank
  private String xpathQuery;

  /**
   * Set the xpath.
   *
   * @param expr
   */
  public void setXpathQuery(String expr) {
    xpathQuery = Args.notBlank(expr, "xpath-query");
  }

  @Override
  public String createXpathQuery(AdaptrisMessage msg) {
    return msg == null ? xpathQuery : msg.resolve(xpathQuery);
  }

  @Override
  public void verify() throws CoreException {
    if (isEmpty(getXpathQuery())) {
      throw new CoreException("Configured Xpath is null.");
    }
  }
}
