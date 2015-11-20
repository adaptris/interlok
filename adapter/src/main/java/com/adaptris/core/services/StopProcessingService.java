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

package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This service will cause the message to not be processed any further and will also request that the Workflows producer not be
 * called.
 * 
 * <p>
 * What happens will be dependent on the parent workflow and parent service collection implementation. See
 * {@link com.adaptris.core.CoreConstants#STOP_PROCESSING_KEY} and {@link com.adaptris.core.CoreConstants#KEY_WORKFLOW_SKIP_PRODUCER} for more information.
 * </p>
 * 
 * @config stop-processing-service
 * 
 */
@XStreamAlias("stop-processing-service")
public class StopProcessingService extends ServiceImp {

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    msg.addMetadata(CoreConstants.STOP_PROCESSING_KEY, CoreConstants.STOP_PROCESSING_VALUE);
    msg.addMetadata(CoreConstants.KEY_WORKFLOW_SKIP_PRODUCER, CoreConstants.STOP_PROCESSING_VALUE);
    log.info("Message will now stop processing");
  }

  @Override
  public void prepare() throws CoreException {
  }


  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }


}
