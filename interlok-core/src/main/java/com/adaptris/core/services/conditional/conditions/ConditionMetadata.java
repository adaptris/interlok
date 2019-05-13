/*
    Copyright Adaptris

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.adaptris.core.services.conditional.conditions;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.conditional.Condition;
import com.adaptris.core.services.conditional.Operator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This {@link Condition} targets message metadata. All you need do is choose an {@link Operator} to
 * apply the conditional test.
 * </p>
 * 
 * @config metadata
 * @author amcgrath
 *
 */
@XStreamAlias("metadata")
@AdapterComponent
@ComponentProfile(summary = "Tests a metadata key against a configured operator.", tag = "condition,metadata")
@DisplayOrder(order = {"metadataKey", "operator"})
public class ConditionMetadata extends ConditionWithOperator {
  
  @NotBlank
  @AffectsMetadata
  private String metadataKey;
  
  @Override
  public boolean evaluate(AdaptrisMessage message) throws CoreException {
    if(!StringUtils.isEmpty(this.getMetadataKey())) {
      log.trace("Testing metadata condition with key: {}", this.getMetadataKey());
      return operator().apply(message, message.getMetadataValue(this.getMetadataKey()));
    } else {
      log.warn("No metadata key supplied, returning false.");
      return false;
    }
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String metadataKey) {
    this.metadataKey = metadataKey;
  }

}
