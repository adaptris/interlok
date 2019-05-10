/*******************************************************************************
 * Copyright 2019 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.adaptris.core.util;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MessageLoggerImpl;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.MetadataLogger;
import com.adaptris.util.NumberUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * MessageLogger &ampl; MetadataLogger implementation that that logs unique-id and metadata but truncates metadata at the
 * configured length.
 * 
 * @config message-logging-with-truncated-metadata
 * @see MessageLogger
 * @see MetadataLogger
 */
@XStreamAlias("message-logging-with-truncated-metadata")
@ComponentProfile(summary = "Log unique-id & metadata (values are truncated) only", since = "3.8.4")
public class TruncateMetadata extends MessageLoggerImpl implements MetadataLogger {

  private static final int DEFAULT_MAX_LENGTH = 256;

  @InputFieldDefault(value = "256")
  private Integer maxLength;

  public TruncateMetadata() {

  }

  public TruncateMetadata(Integer i) {
    this();
    setMaxLength(i);
  }


  @Override
  public String toString(AdaptrisMessage m) {
    return builder(m).append(FIELD_METADATA, format(m.getMetadata())).toString();
  }

  public Integer getMaxLength() {
    return maxLength;
  }

  /**
   * Set the max length for a metadata value before it gets discarded.
   * 
   * @param bytes the length; default is 256 if not specified.
   */
  public void setMaxLength(Integer bytes) {
    this.maxLength = bytes;
  }

  @Override
  public String toString(Collection<MetadataElement> elements) {
    return format(elements).toString();
  }

  private int maxLength() {
    return NumberUtils.toIntDefaultIfNull(getMaxLength(), DEFAULT_MAX_LENGTH);
  }

  @Override
  protected MetadataElement wrap(String key, String value) {
    return new TruncateValue(key, value);
  }

  private class TruncateValue extends MetadataElement {
    private static final long serialVersionUID = 2019040201L;

    public TruncateValue(String key, String value) {
      super(key, value);
    }

    @Override
    public String toString() {
      return String.format("[%s]=[%s]", getKey(),
          StringUtils.abbreviate(getValue(), maxLength()));
    }
  }

}
