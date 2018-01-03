/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.core.metadata;

import java.util.regex.Pattern;

import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Metadata filter implementation that removes metadata where the key matches {@code ^JMS.*$}.
 * 
 * <p>
 * Some brokers dislike any message properties that start with {@code JMS}. For instance, producing a message to WebsphereMQ that
 * contains {@code JMS_isMultiPart} as a string property will fail ({@code JMS_isMultiPart} is populated by SonicMQ when using their
 * {@code MulitpartMessages} implementations). You can configure this on a producer to automatically exclude any JMS style headers
 * from being added. Of course, you could configure the pattern as part of a {@link RegexMetadataFilter} instead.
 * </p>
 * 
 * @config exclude-jms-headers
 */
@XStreamAlias("exclude-jms-headers")
public class ExcludeJmsHeaders extends MetadataFilterImpl {
  /**
   * The Header pattern {@value #JMS_HDR_PATTERN}.
   */
  public static final String JMS_HDR_PATTERN = "^JMS.*$";
  private transient Pattern jmsPattern;

  public ExcludeJmsHeaders() {
    jmsPattern = Pattern.compile(JMS_HDR_PATTERN);
  }

  @Override
  public MetadataCollection filter(MetadataCollection original) {
    return removeJmsHdrs((MetadataCollection) original.clone());
  }

  private MetadataCollection removeJmsHdrs(MetadataCollection metadataCollection) {
    MetadataCollection toBeRemoved = new MetadataCollection();
    for (MetadataElement element : metadataCollection) {
      if (jmsPattern.matcher(element.getKey()).find()) {
        toBeRemoved.add(element);
      }
    }
    metadataCollection.removeAll(toBeRemoved);
    return metadataCollection;
  }
}
