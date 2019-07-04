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

import static com.adaptris.core.marshaller.xstream.XStreamUtils.createParentFields;
import static com.adaptris.core.marshaller.xstream.XStreamUtils.setContainsAnyOf;
import static com.adaptris.core.marshaller.xstream.XStreamUtils.toFieldName;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import com.adaptris.annotation.AnnotationConstants;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StandardStaxDriver;

/**
 * XML output formatter used by XStream to marshal objects.
 * <p>
 * This class converts the object names that are to be used for xml from camel case to a lowercase hyphen separated format.
 * Additionally any class fields configured as CDATA are output as CDATA in the XML.
 * </p>
 * 
 * @author bklair
 */
public class PrettyStaxDriver extends StandardStaxDriver {

  private transient Set<String> cdataFields = new HashSet<String>();
  private transient boolean jdkOnlyStax;

  public PrettyStaxDriver() {
    this(new HashSet<String>(), true);
  }

  public PrettyStaxDriver(Collection<String> pCdataFields, boolean jdkOnlyStax) {
    super();
    this.cdataFields = new HashSet<>(pCdataFields);
    this.jdkOnlyStax = jdkOnlyStax;
  }

  @Override
  public HierarchicalStreamWriter createWriter(OutputStream out) {
    return createWriter(new OutputStreamWriter(out));
  }

  @Override
  public HierarchicalStreamWriter createWriter(Writer out) {

    return new PrettyPrintWriter(out) {
      ArrayList<Class<?>> hierarchy = new ArrayList<Class<?>>();
      boolean cdata = false;

      @SuppressWarnings("rawtypes")
      @Override
      public void startNode(String name, Class clazz) {
        hierarchy.add(clazz);
        startNode(name);
      }

      @Override
      public void endNode() {
        hierarchy.remove(hierarchy.size() - 1);
        super.endNode();
      }

      @Override
      public void startNode(String name) {
        super.startNode(name);
        if (hierarchy.size() >= 2) {
          Class<?> currentClass = hierarchy.get(hierarchy.size() - 2);
          Collection<String> possibles = createParentFields(currentClass,
              toFieldName(name), AnnotationConstants.STANDARD_FIELD_SEPARATOR);
          cdata = setContainsAnyOf(cdataFields, possibles);
        } else {
          cdata = false;
        }
      }

      @Override
      protected void writeText(QuickWriter writer, String text) {
        if (cdata) {
          writer.write("<![CDATA[");
          writer.write(text);
          writer.write("]]>");
        } else {
          super.writeText(writer, text);
        }
      }
    }; // End new PrettyPrintWriter
  }

  @Override
  protected XMLInputFactory createInputFactory() {
    return jdkOnlyStax ? super.createInputFactory() : fallbackInputFactory();
  }

  @Override
  protected XMLOutputFactory createOutputFactory() {
    return jdkOnlyStax ? super.createOutputFactory() : XMLOutputFactory.newInstance();
  }

  // as per StaxDriver.createInputFactor()
  private XMLInputFactory fallbackInputFactory() {
    XMLInputFactory instance = XMLInputFactory.newInstance();
    instance.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    return instance;
  }

}
