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

import java.io.FileOutputStream;
import java.io.OutputStream;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.interlok.types.MessageWrapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MessageWrapper} implementation that wraps an external file as an {@link OutputStream}.
 * 
 * @config file-output-message-wrapper
 * @since 3.9.0
 */
@XStreamAlias("file-output-message-wrapper")
@ComponentProfile(summary = "MessageWrapper implementation that wraps as an OutputStream", since = "3.9.0")
public class FileOutputMessageWrapper extends FileParameter implements MessageWrapper<OutputStream> {


  @Override
  public OutputStream wrap(InterlokMessage m) throws Exception {
    return new FileOutputStream(FsHelper.createFileReference(FsHelper.createUrlFromString(url(m), true)));
  }
}
