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
import com.adaptris.annotation.InterlokAlias;
import com.adaptris.annotation.InterlokLicence;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Null implementation of <code>Service</code>.
 * </p>
 *
 */
@XStreamAlias("null-service")
@InterlokAlias("null-service")
@InterlokLicence(InterlokLicence.Type.NONE)
@AdapterComponent
@ComponentProfile(summary = "A NO-OP service", tag = "service")
public class NullService extends ServiceImp {

  public NullService() {
    super();
  }

  public NullService(String uniqueId) {
    this();
    setUniqueId(uniqueId);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    // do nothing
  }

  @Override
  protected void initService() throws CoreException {
    // do nothing
  }

  @Override
  protected void closeService() {
    // do nothing
  }

  @Override
  public void prepare() throws CoreException {
  }

}
