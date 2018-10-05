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

package com.adaptris.core.runtime;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default null implementation of {@link MessageErrorDigester}.
 * 
 * @config null-message-error-digester
 * @author lchan
 * 
 */
@XStreamAlias("null-message-error-digester")
@AdapterComponent
@ComponentProfile(summary = "A NO-OP message error digester", tag = "error-handling,base")
public class NullMessageErrorDigester extends MessageErrorDigesterImp {

  @Override
  public void digest(AdaptrisMessage message) {
  }

  @Override
  public int getTotalErrorCount() {
    return 0;
  }
}
