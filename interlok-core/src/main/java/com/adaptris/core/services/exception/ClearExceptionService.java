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

package com.adaptris.core.services.exception;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.NullService;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Clears any exception stored against {@value CoreConstants#OBJ_METADATA_EXCEPTION}.
 *
 * @config clear-exception-service
 */
@XStreamAlias("clear-exception-service")
@AdapterComponent
@ComponentProfile(summary = "Clear any exception stored against object metadata",
    tag = "service,error-handling", since = "3.11.0")
public class ClearExceptionService extends NullService {

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    msg.getObjectHeaders().remove(CoreConstants.OBJ_METADATA_EXCEPTION);
  }

}
