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

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.util.URLString;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.IOException;
import java.io.InputStream;

import static com.adaptris.util.URLHelper.connect;

/**
 * {@code DataInputParameter} implementation that returns an input stream to a file.
 * 
 * @config file-input-stream-data-input-parameter
 *
 */
@XStreamAlias("file-input-stream-data-input-parameter")
@DisplayOrder(order = {"destination"})
public class FileInputStreamDataInputParameter extends FileParameter
    implements DataInputParameter<InputStream> {

  @Override
  public InputStream extract(InterlokMessage message) throws CoreException {
    try {
      return connect(new URLString(url(message)));
    } catch (IOException ex) {
      throw ExceptionHelper.wrapCoreException(ex);
    }
  }
}
