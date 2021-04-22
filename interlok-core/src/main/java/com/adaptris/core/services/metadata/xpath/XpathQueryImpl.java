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

import javax.validation.constraints.NotBlank;

import com.adaptris.annotation.InputFieldHint;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;

/**
 * Abstract base class for Metadata Xpath Queries.
 *
 * @author lchan
 *
 */
public abstract class XpathQueryImpl implements XpathMetadataQuery {

  protected transient Logger logR = LoggerFactory.getLogger(this.getClass());

  @Getter
  @Setter
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean allowEmptyResults;

  @Getter
  @NotBlank
  @AffectsMetadata
  private String metadataKey;

  /**
   * Set the metadata key that will be associated with the resolved xpath expression.
   *
   * @param key the key.
   */
  public void setMetadataKey(String key) {
    metadataKey = Args.notBlank(key, "metadataKey");
  }

  protected boolean allowEmptyResults() {
    return BooleanUtils.toBooleanDefaultIfNull(getAllowEmptyResults(), false);
  }
}
