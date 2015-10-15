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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.zip.GZIPOutputStream;

@Deprecated
public class DummyCompressedLogHandler extends MockLogHandler {

  private byte[] compressedBytes;

  public DummyCompressedLogHandler() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    GZIPOutputStream gzip = new GZIPOutputStream(out);
    PrintStream printer = new PrintStream(gzip);
    printer.print(LOG_EXTRACT);
    printer.flush();
    gzip.finish();
    printer.close();
    compressedBytes = out.toByteArray();
  }

  public boolean isCompressed() {
    return true;
  }

  public InputStream retrieveLog(LogFileType type) throws IOException {
    return new ByteArrayInputStream(compressedBytes);
  }
}
