/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.http.oauth;

import java.io.IOException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;

public class DummyAccessTokenBuilder implements AccessTokenBuilder {

  private transient AccessToken token;
  private transient boolean throwError;

  public DummyAccessTokenBuilder() {

  }

  public DummyAccessTokenBuilder(AccessToken t) {
    this(t, false);
  }

  public DummyAccessTokenBuilder(AccessToken t, boolean hasError) {
    this();
    token = t;
    throwError = hasError;
  }

  @Override
  public void init() throws CoreException {  }

  @Override
  public void start() throws CoreException {  }

  @Override
  public void stop() {  }

  @Override
  public void close() {  }

  @Override
  public AccessToken build(AdaptrisMessage msg) throws IOException, CoreException {
    if (throwError) {
      throw new IOException("IOException");
    }
    return token;
  }

}
