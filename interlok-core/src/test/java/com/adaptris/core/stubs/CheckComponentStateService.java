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

package com.adaptris.core.stubs;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.StartedState;

public class CheckComponentStateService extends MockStateManagedComponent implements Service {
  private String uniqueId;
  private Boolean isTrackingEndpoint;
  private Boolean isConfirmation;
  private String lookupName;
  private String comments;

  public CheckComponentStateService() {
    super();
  }

  @Override
  public boolean continueOnFailure() {
    return false;
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    if (retrieveComponentState() != StartedState.getInstance()) {
      throw new ServiceException("Internal state out of step, expected StartedState but was " + retrieveComponentState().toString());
    }
  }

  @Override
  public String createQualifier() {
    return defaultIfEmpty(getUniqueId(), "");
  }

  @Override
  public boolean isBranching() {
    return false;
  }

  @Override
  public void setUniqueId(String id) {
    uniqueId = id;
  }

  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  @Override
  public void setComments(String s) {
    comments = s;
  }

  @Override
  public String getComments() {
    return comments;
  }
  
  @Override
  public String createName() {
    return this.getClass().getName();
  }

  public boolean isConfirmation() {
    return false;
  }

  @Override
  public boolean isTrackingEndpoint() {
    return false;
  }

  public Boolean getIsConfirmation() {
    return isConfirmation;
  }

  public Boolean getIsTrackingEndpoint() {
    return isTrackingEndpoint;
  }

  public void setIsConfirmation(Boolean b) {
    isConfirmation = b;
  }

  public void setIsTrackingEndpoint(Boolean b) {
    isTrackingEndpoint = b;
  }

  public String getLookupName() {
    return lookupName;
  }

  public void setLookupName(String lookupName) {
    this.lookupName = lookupName;
  }
}
