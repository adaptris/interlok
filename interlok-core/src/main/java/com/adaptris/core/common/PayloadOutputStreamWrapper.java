/*
 * Copyright 2015 Adaptris Ltd.
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
 */

package com.adaptris.core.common;

import java.io.OutputStream;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.interlok.types.MessageWrapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MessageWrapper} implementation wraps the payload as an {@link OutputStream}.
 * 
 * @config payload-output-stream-wrapper
 * @since 3.9.0
 */
@XStreamAlias("payload-output-stream-wrapper")
@ComponentProfile(summary = "MessageWrapper implementation wraps the payload as an OutputStream", since = "3.9.0")
public class PayloadOutputStreamWrapper implements MessageWrapper<OutputStream> {

  @Override
  public OutputStream wrap(InterlokMessage m) throws Exception {
    return m.getOutputStream();
  }
}
