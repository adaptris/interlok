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
package com.adaptris.core.services.conditional;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MaxLoopBehaviour} implementation that does nothing.
 * 
 * <p>
 * This means that the message is not touched as it exits the do-while or while loop, no exception is thrown. This effectively
 * preserves the previous behaviour.
 * </p>
 * 
 * @config max-loops-no-behaviour
 *
 */
@XStreamAlias("max-loops-no-behaviour")
@ComponentProfile(summary = "MaxLoopBehaviour implementation that has no behaviour.", since = "3.9.1")
public class OnMaxNoOp implements MaxLoopBehaviour {

  @Override
  public void onMax(AdaptrisMessage msg) throws Exception {
    return;
  }

}
