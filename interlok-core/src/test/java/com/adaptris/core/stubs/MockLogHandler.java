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

import java.io.IOException;

import com.adaptris.core.CoreException;
import com.adaptris.core.LogHandlerImp;

public abstract class MockLogHandler extends LogHandlerImp {

  public static final String LOG_EXTRACT = "TRACE [main] [DownloadPatchRequestEvent.handle()] "
      + "created non-existent directory cfc135ee0000005d0029428e0fa278ab" + System.lineSeparator()
      + "TRACE [main] [DownloadPatchRequestEvent.downloadPatch()] Downloading patch cfc135ee0000005d0029428e0fa278ab "
      + "from [http://development.adaptris.com/internal/core-latest/2010-05-24/HEADER.html]" + System.lineSeparator()
      + "TRACE [main] [DownloadPatchRequestEvent.downloadPatch()] cfc135ee0000005d0029428e0fa278ab written to "
      + "[cfc135ee0000005d0029428e0fa278ab/HEADER.html]" + System.lineSeparator()
      + "TRACE [main] [DownloadPatchRequestEvent.handle()] created non-existent directory cfc137080000005d0029428e4812f3e9"
      + System.lineSeparator();

  public void clean() throws IOException {
  }

  @Override
  public void prepare() throws CoreException {
  }

}
