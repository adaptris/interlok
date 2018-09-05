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

import java.util.ArrayList;
import java.util.Collection;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.util.LoggingHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This is a dummy for marshalling purposes.
 * 
 * @config no-retries
 * 
 * @author lchan
 * 
 */
@XStreamAlias("no-retries")
@AdapterComponent
@ComponentProfile(summary = "The default NO-OP failed message retrier implementation", tag = "error-handling,base")
public class NoRetries extends AdaptrisComponentImp implements FailedMessageRetrier {
  
  private String uniqueId;

  public void addWorkflow(Workflow workflow) {
    ;
  }

  public void close() {
  }

  public void init() throws CoreException {
  }

  public void start() throws CoreException {
  }

  public void stop() {
  }

  public void onAdaptrisMessage(AdaptrisMessage msg) {
  }

  public void clearWorkflows() {
  }

  public Collection<String> registeredWorkflowIds() {
    return new ArrayList<String>();
  }

  @Override
  public void prepare() throws CoreException {

  }

  public String getUniqueId() {
    return uniqueId;
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  @Override
  public String friendlyName() {
    return LoggingHelper.friendlyName(this);
  }

}
