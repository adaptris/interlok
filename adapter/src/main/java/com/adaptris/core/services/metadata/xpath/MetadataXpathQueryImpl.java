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

import static org.apache.commons.lang.StringUtils.isEmpty;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;

/**
 * Abstract base class for {@linkplain XpathQuery} implementations that derive their xpath query from metadata.
 *
 * @author lchan
 *
 */
public abstract class MetadataXpathQueryImpl extends XpathQueryImpl {

  @NotBlank
  private String xpathMetadataKey;

  public MetadataXpathQueryImpl() {
  }

  public String getXpathMetadataKey() {
    return xpathMetadataKey;
  }

  /**
   * Set the xpath.
   *
   * @param expr
   */
  public void setXpathMetadataKey(String expr) {
    xpathMetadataKey = Args.notBlank(expr, "xpath-metadata-key");
  }

  @Override
  public String createXpathQuery(AdaptrisMessage msg) throws CoreException {
    String xpath = msg.getMetadataValue(getXpathMetadataKey());
    if (isEmpty(xpath)) {
      throw new CoreException(getXpathMetadataKey() + " does not exist as metadata or is null");
    }
    return xpath;
  }

  public void verify() throws CoreException {
    if (isEmpty(getXpathMetadataKey())) {
      throw new CoreException("Configured Xpath Metadata is null.");
    }
  }
}
