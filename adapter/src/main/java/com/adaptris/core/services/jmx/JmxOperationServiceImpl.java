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

package com.adaptris.core.services.jmx;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.CoreException;
import com.adaptris.core.jmx.JmxConnection;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;

/**
 * Base abstract implementation for all JMX services.
 * 
 * @since 3.3.0
 */
public abstract class JmxOperationServiceImpl extends JmxOperationImpl {
  
  @Valid
  @NotNull
  @AutoPopulated
  private AdaptrisConnection connection;

  public JmxOperationServiceImpl() {
    super();
    setConnection(new JmxConnection());
  }

  @Override
  public void prepare() throws CoreException {
    LifecycleHelper.prepare(getConnection());
  }

  @Override
  protected void initService() throws CoreException {
    super.initService();
    LifecycleHelper.init(getConnection());
  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(getConnection());
  }

  @Override
  public void stop() {
    super.stop();
    LifecycleHelper.stop(getConnection());
  }

  @Override
  public void start() throws CoreException {
    super.start();
    LifecycleHelper.start(getConnection());
  }

  /**
   * @return the connection
   */
  public AdaptrisConnection getConnection() {
    return connection;
  }

  /**
   * @param c the connection to set
   */
  public void setConnection(AdaptrisConnection c) {
    this.connection = Args.notNull(c, "connection");
  }
}
