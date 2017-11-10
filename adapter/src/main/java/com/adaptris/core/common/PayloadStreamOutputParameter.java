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

import static com.adaptris.util.stream.StreamUtil.copyAndClose;
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.IOException;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataOutputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This {@code DataOutputParameter} is used when you want to write data to the {@link com.adaptris.core.AdaptrisMessage} payload.
 * 
 * 
 * @config stream-payload-output-parameter
 * 
 */
@XStreamAlias("stream-payload-output-parameter")
@DisplayOrder(order = {"contentEncoding"})
public class PayloadStreamOutputParameter implements DataOutputParameter<InputStreamWithEncoding> {

  @AdvancedConfig
  private String contentEncoding;

  @Override
  public void insert(InputStreamWithEncoding data, InterlokMessage msg) throws InterlokException {
    try {
      String encoding = defaultIfEmpty(getContentEncoding(), data.encoding);
      if (isEmpty(encoding)) {
        copyAndClose(data.inputStream, msg.getOutputStream());
      } else {
        copyAndClose(data.inputStream, msg.getWriter(encoding));
        msg.setContentEncoding(encoding);
      }
    } catch (IOException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  public String getContentEncoding() {
    return contentEncoding;
  }

  public void setContentEncoding(String contentEncoding) {
    this.contentEncoding = contentEncoding;
  }
}
