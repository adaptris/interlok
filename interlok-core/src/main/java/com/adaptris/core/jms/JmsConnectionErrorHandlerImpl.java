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

package com.adaptris.core.jms;

import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.ConnectionErrorHandler;
import com.adaptris.core.ConnectionErrorHandlerImp;
import com.adaptris.core.CoreException;

public abstract class JmsConnectionErrorHandlerImpl extends ConnectionErrorHandlerImp {

  protected transient String idForLogging;

  @Override
  public void init() throws CoreException {
    String loggingId = retrieveConnection(AdaptrisConnection.class).getUniqueId();
    if (!isEmpty(loggingId)) {
      idForLogging = loggingId;
    }
    else {
      idForLogging = abbreviate(retrieveConnection(JmsConnection.class).getBrokerDetailsForLogging(), 20);
    }
  }

  @Override
  public void handleConnectionException() {
    super.restartAffectedComponents();
  }

  @Override
  public boolean allowedInConjunctionWith(ConnectionErrorHandler ceh) {
    return !equals(ceh);
  }

  @Override
  public boolean equals(Object obj) {
    boolean result = false;
    if (obj != null) {
      if (obj instanceof JmsConnectionErrorHandlerImpl) {
        JmsConnection other = ((JmsConnectionErrorHandlerImpl) obj).retrieveConnection(JmsConnection.class);
        JmsConnection thisConnection = retrieveConnection(JmsConnection.class);
        
        result = thisConnection.connectionEquals(other);
      }
    }
    return result;
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    if (retrieveConnection(JmsConnection.class) != null) {
      hashCode = new HashCodeBuilder(11, 17).append(retrieveConnection(JmsConnection.class)).toHashCode();
    }
    return hashCode == 0 ? super.hashCode() : hashCode;
  }
}
