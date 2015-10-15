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

import static org.apache.commons.lang.StringUtils.isEmpty;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BranchingServiceImp;
import com.adaptris.core.ServiceException;
import com.adaptris.util.GuidGenerator;

// Basically we use this to verify that the the unique-id has been used to marshalled Identity
// i.e. marshalledIdentity has never been set.
// when we are running through the service.
// - http://engineering.adaptris.net/redmine/issues/1681
public class XmlRoundTripService extends BranchingServiceImp {

  private String marshalledIdentity;

  private transient String uniqueIdentity;

  public XmlRoundTripService() {
    uniqueIdentity = new GuidGenerator().getUUID();
  }

  @Override
  public void init() {
    if (isEmpty(marshalledIdentity)) {
      setServiceIdentity(uniqueIdentity);
    }
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    if (!uniqueIdentity.equals(marshalledIdentity)) {
      throw new ServiceException("Unique-Identity != Marshalled-Identity");
    }
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
    // na
  }

  public String getServiceIdentity() {
    return marshalledIdentity;
  }

  public void setServiceIdentity(String serviceIdentity) {
    marshalledIdentity = serviceIdentity;
  }

}
