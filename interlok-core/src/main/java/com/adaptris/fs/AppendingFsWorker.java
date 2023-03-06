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

package com.adaptris.fs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Implementation of {@link FsWorker} that appeneds to the file for any write operations.
 * 
 * @config fs-append-file
 */
@JacksonXmlRootElement(localName = "fs-append-file")
@XStreamAlias("fs-append-file")
public class AppendingFsWorker extends StandardWorker {

  @Override
  public void put(byte[] data, File file) throws FsException {
    try (OutputStream out = new FileOutputStream(file, true)) {
      out.write(data);
    }
    catch (Exception e) {
      throw wrapException(e);
    }
  }
}
