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
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MaxLoopBehaviour} implementation that throws a {@link ServiceException}.
 * 
 * <p>
 * When the maximum number of loops is hit; an exception is thrown.
 * </p>
 * 
 * @config max-loops-throw-exception
 *
 */
@XStreamAlias("max-loops-throw-exception")
@ComponentProfile(summary = "MaxLoopBehaviour implementation that throws a ServiceException.", since = "3.9.1")
public class OnMaxThrowException implements MaxLoopBehaviour {

  @Override
  public void onMax(AdaptrisMessage msg) throws Exception {
    throw new ServiceException("Exceeded Max Loops");
  }

}
