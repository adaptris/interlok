/*
 * Copyright 2019 Adaptris Ltd.
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

import static com.adaptris.util.URLHelper.connect;

import java.io.InputStream;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.interlok.types.MessageWrapper;
import com.adaptris.util.URLString;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MessageWrapper} implementation that wraps an external file as an {@link InputStream}.
 * 
 * @config file-input-message-wrapper
 * @since 3.9.0
 */
@XStreamAlias("file-input-message-wrapper")
@ComponentProfile(summary = "MessageWrapper implementation that wraps as an InputStream", since = "3.9.0")
public class FileInputMessageWrapper extends FileParameter implements MessageWrapper<InputStream> {


  @Override
  public InputStream wrap(InterlokMessage m) throws Exception {
    return connect(new URLString(url(m)));
  }

}
