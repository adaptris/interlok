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

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataOutputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This {@code DataOutputParameter} is used when you want to write some data to {@link com.adaptris.core.AdaptrisMessage}
 * metadata.
 * 
 * @config metadata-stream-output-parameter
 * 
 */
@XStreamAlias("metadata-stream-output-parameter")
@DisplayOrder(order = {"metadataKey", "contentEncoding"})
public class MetadataStreamOutputParameter implements DataOutputParameter<InputStream> {
  
  static final String DEFAULT_METADATA_KEY = "destinationKey";

  @NotBlank
  @AutoPopulated
  private String metadataKey;
  @AdvancedConfig
  private String contentEncoding;
  
  public MetadataStreamOutputParameter() {
    this.setMetadataKey(DEFAULT_METADATA_KEY);
  }
  
  public MetadataStreamOutputParameter(String key) {
    this();
    setMetadataKey(key);
  }

  @Override
  public void insert(InputStream data, InterlokMessage message) throws InterlokException {
    try {
      StringBuilder builder = new StringBuilder();
      try (Reader in = getReader(data, defaultIfEmpty(getContentEncoding(), message.getContentEncoding()));
          StringBuilderWriter out = new StringBuilderWriter(builder)) {
        IOUtils.copy(in, out);
      }
      message.addMessageHeader(getMetadataKey(), builder.toString());
    } catch (IOException e) {
      ExceptionHelper.rethrowCoreException(e);
    }
  }

  private InputStreamReader getReader(InputStream in, String encoding) throws UnsupportedEncodingException {
    InputStreamReader reader = null;
    if (encoding == null) {
      reader = new InputStreamReader(in);
    } else {
      reader = new InputStreamReader(in, encoding);
    }
    return reader;
  }


  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String key) {
    this.metadataKey = Args.notBlank(key, "metadata key");
  }

  public String getContentEncoding() {
    return contentEncoding;
  }

  public void setContentEncoding(String contentEncoding) {
    this.contentEncoding = contentEncoding;
  }

}
