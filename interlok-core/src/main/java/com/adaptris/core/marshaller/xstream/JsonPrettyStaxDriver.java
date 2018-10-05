/*
 * Copyright 2015 Adaptris Ltd.
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
 */

package com.adaptris.core.marshaller.xstream;

import java.io.OutputStream;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedXMLOutputFactory;

import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JettisonStaxWriter;
import com.thoughtworks.xstream.io.xml.QNameMap;
import com.thoughtworks.xstream.io.xml.StaxWriter;

/**
 * Json Pretty Driver.
 * <p>
 * This class is a clone of com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver, the only difference is in the constructor
 * where we create a custom MappedXMLOutputFactory.
 * </p>
 * 
 * @author bklair
 */
public class JsonPrettyStaxDriver extends JettisonMappedXmlDriver {

  protected MappedXMLOutputFactory myOutputFactory;

  public JsonPrettyStaxDriver() {
    this(new Configuration());
  }

  public JsonPrettyStaxDriver(final Configuration config) {
    this(config, true);
  }

  public JsonPrettyStaxDriver(final Configuration config, final boolean useSerializeAsArray) {
    super(config, useSerializeAsArray);
    myOutputFactory = new MappedXMLOutputFactory(config) {
      @Override
      public XMLStreamWriter createXMLStreamWriter(Writer writer) throws XMLStreamException {
        return new PrettyMappedJSONStreamWriter(convention, writer);
      }
    };
  }

  @Override
  public HierarchicalStreamWriter createWriter(final Writer writer) {
    try {
      if (useSerializeAsArray) {
        return new JettisonStaxWriter(new QNameMap(), myOutputFactory.createXMLStreamWriter(writer), getNameCoder(), convention);
      } else {
        return new StaxWriter(new QNameMap(), myOutputFactory.createXMLStreamWriter(writer), getNameCoder());
      }
    } catch (final XMLStreamException e) {
      throw new StreamException(e);
    }
  }

  @Override
  public HierarchicalStreamWriter createWriter(final OutputStream output) {
    try {
      if (useSerializeAsArray) {
        return new JettisonStaxWriter(new QNameMap(), myOutputFactory.createXMLStreamWriter(output), getNameCoder(), convention);
      }
      else {
        return new StaxWriter(new QNameMap(), myOutputFactory.createXMLStreamWriter(output), getNameCoder());
      }
    }
    catch (final XMLStreamException e) {
      throw new StreamException(e);
    }
  }

}
