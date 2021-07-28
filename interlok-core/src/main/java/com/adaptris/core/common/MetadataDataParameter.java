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

package com.adaptris.core.common;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.config.DataDestination;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * <p>
 * This {@code DataDestination} is used when you want to source
 * data from the {@link com.adaptris.core.AdaptrisMessage} metadata and
 * put the result back there.
 * </p>
 * <p>
 * An example might be specifying that the XPath expression required for the {@link
 * com.adaptris.core.services.path.XPathService} can be found in
 * a particular metadata item of an {@link com.adaptris.core.AdaptrisMessage}.
 * </p>
 * 
 * @author amanderson
 * @config metadata-data-parameter
 * 
 */
@XStreamAlias("metadata-data-parameter")
@DisplayOrder(order = {"metadataKey"})
public class MetadataDataParameter implements DataDestination<String, String>
{
  static final String DEFAULT_METADATA_KEY = "metadata-key";

  @Getter
  @NotNull
  @AutoPopulated
  @Valid
  private String metadataKey;

  public MetadataDataParameter()
  {
    this(DEFAULT_METADATA_KEY);
  }

  public MetadataDataParameter(String key)
  {
    metadataKey = key;
  }

  public void setMetadataKey(String key)
  {
    metadataKey = Args.notBlank(key, "metadata key");
  }

  @Override
  public String extract(InterlokMessage message)
  {
    return message.getMessageHeaders().get(metadataKey);
  }

  @Override
  public void insert(String data, InterlokMessage message)
  {
    message.addMessageHeader(metadataKey, data);
  }
}
