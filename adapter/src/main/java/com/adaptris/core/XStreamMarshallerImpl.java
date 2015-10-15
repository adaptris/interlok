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

import java.io.Reader;
import java.io.Writer;

import com.thoughtworks.xstream.XStream;

/**
 * Abstract XStream version of {@link AdaptrisMarshaller}
 *
 * @author D Sefton
 */
public abstract class XStreamMarshallerImpl extends AbstractMarshaller{

  protected transient XStream instance;

  /**
   * Typically it will do something like this:-
   * 
   * <pre>
   * {@code 
   * if (instance == null){
   *    create and configure a new instance
   * }
   * return instance;
   * }
   * </pre>
   * 
   * @return a pre-configured instance.
   * 
   */
  protected abstract XStream getInstance();

  @Override
  public String marshal(Object obj) throws CoreException {
    String xmlResult = getInstance().toXML(obj);
    return xmlResult;
  }

  @Override
  public void marshal(Object obj, Writer writer) throws CoreException {
    try {
      getInstance().toXML(obj, writer);
      writer.flush();
    }
    catch (Exception ex) {
      throw new CoreException(ex);
    }
  }

  @Override
  public Object unmarshal(Reader reader) throws CoreException {
    Object result = null;
    try {
      result = getInstance().fromXML(reader);
      reader.close();

    }
    catch (Exception e) {
      throw new CoreException(e);
    }

    return result;
  }
}
