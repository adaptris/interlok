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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.URLHelper;
import com.adaptris.util.URLString;

/**
 * Abstract implementation of AdaptrisMarshaller.
 *
 * @author lchan
 *
 */
public abstract class AbstractMarshaller implements AdaptrisMarshaller {

  /**
   * @see AdaptrisMarshaller#marshal(java.lang.Object, java.lang.String)
   */
  @Override
  public void marshal(Object obj, String fileName) throws CoreException {
    this.marshal(obj, new File(fileName));
  }

  /**
   * @see AdaptrisMarshaller#marshal(java.lang.Object, File)
   */
  @Override
  public void marshal(Object obj, File fileName) throws CoreException {
    invokeSerialize(() -> {
      try (Writer writer = new FileWriter(fileName)) {
        marshal(obj, writer);
      }      
    });
  }

  /**
   * @see AdaptrisMarshaller#marshal(java.lang.Object, OutputStream)
   */
  @Override
  public void marshal(Object obj, OutputStream outputStream) throws CoreException {
    this.marshal(obj, new OutputStreamWriter(outputStream));
  }

  /**
   * @see AdaptrisMarshaller#marshal(java.lang.Object, java.net.URL)
   */
  @Override
  public void marshal(Object obj, URL fileUrl) throws CoreException {
    Args.notNull(obj, "object");
    Args.notNull(fileUrl, "fileUrl");
    if (!fileUrl.getProtocol().equals("file")) { // URL can only be file
      throw new CoreException("URL protocol must be file:");
    }
    this.marshal(obj, new File(fileUrl.getFile()));
  }

  /**
   * @see AdaptrisMarshaller#unmarshal(java.lang.String)
   */
  @Override
  public Object unmarshal(String xml) throws CoreException {
    return invokeDeserialize(() -> {
      try (Reader reader = new StringReader(xml)) {
        return unmarshal(reader);
      }
    });
  }

  /**
   * @see AdaptrisMarshaller#unmarshal(java.io.File)
   */
  @Override
  public Object unmarshal(File file) throws CoreException {
    return invokeDeserialize(() -> {
      try (FileReader reader = new FileReader(file)) {
        return unmarshal(reader);
      }
    });
  }

  /**
   * @see AdaptrisMarshaller#unmarshal(java.net.URL)
   */
  @Override
  public Object unmarshal(URL url) throws CoreException {
    Args.notNull(url, "fileUrl");
    return invokeDeserialize(() -> {
      try (InputStream in = url.openStream()) {
        return this.unmarshal(in);
      }
    });
  }

  @Override
  public Object unmarshal(URLString loc) throws CoreException {
    return invokeDeserialize(() -> {
      try (InputStream in = URLHelper.connect(loc)) {
        return unmarshal(in);
      }
    });
  }

  @Override
  public Object unmarshal(InputStream stream) throws CoreException {
    return this.unmarshal(new InputStreamReader(stream));
  }

  /**
   * Wrap the unmarshalling sequence by catching exceptions and re-throwing as CoreExceptions.
   * 
   */
  protected static Object invokeDeserialize(Deserializer u) throws CoreException {
    try {
      return u.deserialize();
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }
  
  /**
   * Wrap the marshalling sequence by catching exceptions and re-throwing as CoreExceptions.
   * 
   */
  protected static void invokeSerialize(Serializer m) throws CoreException {
    try {
      m.serialize();
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @FunctionalInterface
  protected interface Deserializer {
    Object deserialize() throws Exception;
  }

  @FunctionalInterface
  protected interface Serializer {
    void serialize() throws Exception;
  }
}
