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

package com.adaptris.core.services.splitter;

import java.util.Collections;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A {@link MessageSplitter} implementation that doesn't actually split.
 * 
 * <p>
 * Effectively, using this splitter implementation just returns the original message as the split message on a 1:1 basis
 * </p>
 * 
 * @config no-op-splitter
 */
@XStreamAlias("no-op-splitter")
public class NoOpSplitter implements MessageSplitter {

  public NoOpSplitter() {
  }

  @Override
  public List<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException {
    return Collections.singletonList(msg);
  }
}
