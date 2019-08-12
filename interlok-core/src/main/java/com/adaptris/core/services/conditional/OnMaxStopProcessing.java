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
import com.adaptris.core.CoreConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MaxLoopBehaviour} implementation that marks a message with metadata that stop processing.
 * 
 * <p>
 * What happens will be dependent on the parent workflow and parent service collection implementation. But this implementation will
 * mark the message with the metadata keys {@link com.adaptris.core.CoreConstants#STOP_PROCESSING_KEY} and
 * {@link com.adaptris.core.CoreConstants#KEY_WORKFLOW_SKIP_PRODUCER}.
 * </p>
 * </p>
 * 
 * @config max-loops-stop-processing
 *
 */
@XStreamAlias("max-loops-stop-processing")
@ComponentProfile(summary = "MaxLoopBehaviour implementation that marks a message with 'stop-processing' flags", since = "3.9.1")
public class OnMaxStopProcessing implements MaxLoopBehaviour {

  @Override
  public void onMax(AdaptrisMessage msg) throws Exception {
    msg.addMetadata(CoreConstants.STOP_PROCESSING_KEY, CoreConstants.STOP_PROCESSING_VALUE);
    msg.addMetadata(CoreConstants.KEY_WORKFLOW_SKIP_PRODUCER, CoreConstants.STOP_PROCESSING_VALUE);
  }


}
