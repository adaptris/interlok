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

package com.adaptris.core.services.splitter;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link MessageSplitter} which creates multiple instances of the same document based on a metadata key.
 * 
 * <p>
 * The split messages will always contain the entire document, the metadata key containing the number of documents to generate.
 * </p>
 * 
 * @config metadata-document-copier
 */
@XStreamAlias("metadata-document-copier")
@DisplayOrder(order = {"metadataKey", "copyMetadata", "copyObjectMetadata", "indexMetadataKey"})
public class MetadataDocumentCopier extends MessageCopier {

  @NotNull
  @NotBlank
  private String metadataKey = null;
  @AdvancedConfig
  private String indexMetadataKey;

  public MetadataDocumentCopier() {
  }


  public MetadataDocumentCopier(String key) {
    this(key, null);
  }

  public MetadataDocumentCopier(String key, String indexKey) {
    this();
    setMetadataKey(key);
    setIndexMetadataKey(indexKey);
  }

  @Override
  public com.adaptris.core.util.CloseableIterable<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException {
    int size = toInteger(msg.getMetadataValue(getMetadataKey()));     
    return new MessageCopierIterator(msg, size, (m, count) -> {
      if (!isEmpty(getIndexMetadataKey())) {
        m.addMetadata(getIndexMetadataKey(), String.valueOf(count));
      }
      return m;
    });
  }

  /**
   * Set the metadata key to use to for generating msgs.
   *
   * @param key the metadata key
   */
  public void setMetadataKey(String key) {
    metadataKey = Args.notEmpty(key, "metadata key");
  }

  public String getMetadataKey() {
    return metadataKey;
  }


  public String getIndexMetadataKey() {
    return indexMetadataKey;
  }

  /**
   * The metadata key storing the current index of the message from {@link #setMetadataKey(String)}
   * will be stored (zero based index).
   * 
   * @param s the index metadata key to set
   */
  public void setIndexMetadataKey(String s) {
    indexMetadataKey = s;
  }
}
