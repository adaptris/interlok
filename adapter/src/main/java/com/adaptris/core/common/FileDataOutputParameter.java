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

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MessageDrivenDestination;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.config.DataOutputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@code DataInputParameter} implementation that writes to a file.
 * 
 * @config file-data-output-parameter
 */
@XStreamAlias("file-data-output-parameter")
@DisplayOrder(order = {"destination", "url"})
public class FileDataOutputParameter implements DataOutputParameter<String> {
  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @Deprecated
  @AdvancedConfig
  private String url;

  @Valid
  private MessageDrivenDestination destination;


  public FileDataOutputParameter() {

  }

  @Override
  public void insert(String data, InterlokMessage message) throws CoreException {
    OutputStream out = null;
    try {
      URL url = FsHelper.createUrlFromString(this.url(message), true);
      out = new FileOutputStream(FsHelper.createFileReference(url));
      IOUtils.write((String) data, out, message.getContentEncoding());
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    } finally {
      IOUtils.closeQuietly(out);
    }
  }

  protected String url(InterlokMessage msg) throws CoreException{
    if (getDestination() != null) {
      if (msg instanceof AdaptrisMessage) {
        return getDestination().getDestination((AdaptrisMessage)msg);
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
    this.url = url;
  }

  public MessageDrivenDestination getDestination() {
    return destination;
  }

  /**
   * Set the destination for the file data output.
   *
   * @param d the destination.
   */
  public void setDestination(MessageDrivenDestination d) {
    destination = Args.notNull(d, "destination");
  }


}
