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

package com.adaptris.core.marshaller.xstream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLInputFactory;
import org.codehaus.jettison.mapped.MappedXMLOutputFactory;

import com.thoughtworks.xstream.io.AbstractDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.json.JettisonStaxWriter;
import com.thoughtworks.xstream.io.xml.QNameMap;
import com.thoughtworks.xstream.io.xml.StaxReader;
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
public class JsonPrettyStaxDriver extends AbstractDriver {

  protected final MappedXMLOutputFactory mof;
  protected final MappedXMLInputFactory mif;
  protected final MappedNamespaceConvention convention;
  protected final boolean useSerializeAsArray;
  
  
  public JsonPrettyStaxDriver() {
    Configuration configuration = new Configuration();
    mof = new MappedXMLOutputFactory(configuration) {
      @Override
      public XMLStreamWriter createXMLStreamWriter(Writer writer) throws XMLStreamException {
        return new PrettyMappedJSONStreamWriter(convention, writer);
      }
    };
    
    mif = new MappedXMLInputFactory(configuration);
    convention = new MappedNamespaceConvention(configuration);
    useSerializeAsArray = true;
  }
  
  @Override
  public HierarchicalStreamReader createReader(final Reader reader) {
      try {
          return new StaxReader(new QNameMap(), mif.createXMLStreamReader(reader), getNameCoder());
      } catch (final XMLStreamException e) {
          throw new StreamException(e);
      }
  }

  @Override
  public HierarchicalStreamReader createReader(final InputStream input) {
      try {
          return new StaxReader(new QNameMap(), mif.createXMLStreamReader(input), getNameCoder());
      } catch (final XMLStreamException e) {
          throw new StreamException(e);
      }
  }

  @Override
  public HierarchicalStreamReader createReader(URL in) {
      InputStream instream = null;
      try {
          instream = in.openStream();
          return new StaxReader(new QNameMap(), mif.createXMLStreamReader(
              in.toExternalForm(), instream), getNameCoder());
      } catch (final XMLStreamException e) {
          throw new StreamException(e);
      } catch (IOException e) {
          throw new StreamException(e);
      } finally {
          if (instream != null) {
              try {
                  instream.close();
              } catch (IOException e) {
                  // ignore
              }
          }
      }
  }

  @Override
  public HierarchicalStreamReader createReader(File in) {
      InputStream instream = null;
      try {
          instream = new FileInputStream(in);
          return new StaxReader(new QNameMap(), mif.createXMLStreamReader(in
              .toURI()
              .toASCIIString(), instream), getNameCoder());
      } catch (final XMLStreamException e) {
          throw new StreamException(e);
      } catch (IOException e) {
          throw new StreamException(e);
      } finally {
          if (instream != null) {
              try {
                  instream.close();
              } catch (IOException e) {
                  // ignore
              }
          }
      }
  }

  @Override
  public HierarchicalStreamWriter createWriter(final Writer writer) {
      try {
          if (useSerializeAsArray) {
              return new JettisonStaxWriter(new QNameMap(), mof.createXMLStreamWriter(writer), getNameCoder(), convention);
          } else {
              return new StaxWriter(new QNameMap(), mof.createXMLStreamWriter(writer), getNameCoder());
          }
      } catch (final XMLStreamException e) {
          throw new StreamException(e);
      }
  }

  @Override
  public HierarchicalStreamWriter createWriter(final OutputStream output) {
      try {
          if (useSerializeAsArray) {
              return new JettisonStaxWriter(new QNameMap(), mof.createXMLStreamWriter(output), getNameCoder(), convention);
          } else {
              return new StaxWriter(new QNameMap(), mof.createXMLStreamWriter(output), getNameCoder());
          }
      } catch (final XMLStreamException e) {
          throw new StreamException(e);
      }
  }
}
