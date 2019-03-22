/*******************************************************************************
 * Copyright 2019 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.adaptris.core.services.dynamic;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;

public abstract class ExtractorWithConnection implements ServiceExtractor {
  @NotNull
  @Valid
  private AdaptrisConnection connection;


  public ExtractorWithConnection() {

  }

  @Override
  public void init() throws CoreException {
    try {
      Args.notNull(getConnection(), "connection");
      LifecycleHelper.prepare(getConnection());
      LifecycleHelper.init(getConnection());
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(getConnection());
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(getConnection());
  }

  @Override
  public void close() {
    LifecycleHelper.close(getConnection());
  }

  public AdaptrisConnection getConnection() {
    return connection;
  }

  public void setConnection(AdaptrisConnection c) {
    this.connection = Args.notNull(c, "connection");
  }

  public <T extends ExtractorWithConnection> T withConnection(AdaptrisConnection c) {
    setConnection(c);
    return (T) this;
  }
}
