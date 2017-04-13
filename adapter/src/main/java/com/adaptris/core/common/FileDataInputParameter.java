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

import java.io.IOException;

import javax.validation.Valid;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MessageDrivenDestination;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.util.URLString;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@code DataInputParameter} implementation that reads from a file.
 * 
 * @config file-data-input-parameter
 *
 */
@XStreamAlias("file-data-input-parameter")
@DisplayOrder(order = {"destination","url"})
public class FileDataInputParameter extends FileInputParameterImpl {

  @Deprecated
  private String url;

  @Valid
  private MessageDrivenDestination destination;

  public FileDataInputParameter() {

  }

  @Override
  public String extract(InterlokMessage message) throws CoreException {
    try {
      return this.load(new URLString(this.url(message)), message.getContentEncoding());
    } catch (IOException ex) {
      throw new CoreException(ex);
    }
  }

  protected String url(InterlokMessage msg) throws CoreException {
    if (getDestination() != null) {
      if (msg instanceof AdaptrisMessage) {
        return getDestination().getDestination((AdaptrisMessage) msg);
      } else {
        throw new RuntimeException("Message is not instance of Adaptris Message");
      }
    }
    log.warn("[url] is deprecated, use [destination] instead");
    return getUrl();
  }

  /**
   * @deprecated since 3.5.0 use {@link #getDestination()} instead for consistency.
   */
  @Deprecated
  public String getUrl() {
    return url;
  }

  /**
   * @deprecated since 3.5.0 use {@link #setDestination(MessageDrivenDestination)} instead for consistency.
   */
  @Deprecated
  public void setUrl(String url) {
    this.url = Args.notBlank(url, "url");
  }

  public MessageDrivenDestination getDestination() {
    return destination;
  }

  /**
   * Set the destination for the file data input.
   *
   * @param d the destination.
   */
  public void setDestination(MessageDrivenDestination d) {
    if (d == null) {
      throw new IllegalArgumentException("Destination is null");
    }
    destination = d;
  }

}
