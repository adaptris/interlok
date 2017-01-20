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

package com.adaptris.core.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;

import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.util.URLString;

public abstract class FileInputParameterImpl implements DataInputParameter<String> {

  public FileInputParameterImpl() {}

  protected String load(URLString loc, String encoding) throws IOException {
    String content = null;

    try (InputStream inputStream = connectToInputUrl(loc)) {
      StringWriter writer = new StringWriter();
      IOUtils.copy(inputStream, writer, encoding);
      content = writer.toString();
    }
    return content;
  }

  private InputStream connectToInputUrl(URLString loc) throws IOException {
    if (loc.getProtocol() == null || "file".equals(loc.getProtocol())) {
      return connectToInputFile(loc.getFile());
    }
    URL url = new URL(loc.toString());
    URLConnection conn = url.openConnection();
    return conn.getInputStream();
  }

  private InputStream connectToInputFile(String localFile) throws IOException {
    InputStream in = null;
    File f = new File(localFile);
    if (f.exists()) {
      in = new FileInputStream(f);
    } else {
      ClassLoader c = this.getClass().getClassLoader();
      URL u = c.getResource(localFile);
      if (u != null) {
        in = u.openStream();
      }
    }
    if(in == null) {
      throw new FileNotFoundException(localFile);
    }
    return in;
  }
}
