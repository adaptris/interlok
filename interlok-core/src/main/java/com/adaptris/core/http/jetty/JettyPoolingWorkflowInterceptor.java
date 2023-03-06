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

package com.adaptris.core.http.jetty;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * WorkflowInterceptor implementation that allows a Jetty Consumer to be part of a {@link com.adaptris.core.PoolingWorkflow}.
 * 
 * @config jetty-pooling-workflow-interceptor
 * 
 */
@JacksonXmlRootElement(localName = "jetty-pooling-workflow-interceptor")
@XStreamAlias("jetty-pooling-workflow-interceptor")
@AdapterComponent
@ComponentProfile(summary = "Interceptor that allows a jetty consumer to be part of a PoolingWorkflow",
    tag = "interceptor,http,https")
public class JettyPoolingWorkflowInterceptor extends JettyWorkflowInterceptorImpl {

  public JettyPoolingWorkflowInterceptor() {
    super();
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
  }

  @Override
  public void workflowStart(AdaptrisMessage inputMsg) {
  }

  @Override
  public void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    endWorkflow(inputMsg, outputMsg);
  }

}
