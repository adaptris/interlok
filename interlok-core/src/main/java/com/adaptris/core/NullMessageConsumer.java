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

package com.adaptris.core;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Null implementation of <code>AdaptrisMessageConsumer</code>.
 * </p>
 *
 * @config null-message-consumer
 */
@XStreamAlias("null-message-consumer")
@AdapterComponent
@ComponentProfile(summary = "Default NO-OP consumer implementation", tag = "consumer,base", recommended = {NullConnection.class})
public class NullMessageConsumer extends AdaptrisMessageConsumerImp {

  public NullMessageConsumer() {
    setMessageFactory(null);
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected String newThreadName() {
    AdaptrisMessageListener listener = retrieveAdaptrisMessageListener();
    return listener != null ? listener.friendlyName() : this.createName();
  }

}
