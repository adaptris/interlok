/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.core.lms;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

abstract class StreamWrapper {

  protected abstract OutputStream asOutputStream(File f, Callback c) throws IOException;

  protected abstract InputStream asInputStream(File f, Callback c) throws IOException;

  @FunctionalInterface
  public interface Callback {
    void nowClosed();
  }
}
