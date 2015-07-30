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

import com.adaptris.annotation.AnnotationConstants;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * XML output formatter used by XStream to marshal objects.
 * <p>
 * This class converts the object names that are to be used for xml from camel case to a lowercase hyphen separated format.
 * Additionally any class fields configured as CDATA are output as CDATA in the XML.
 * </p>
 * 
 * @author bklair
 */
public class PrettyStaxDriver extends StaxDriver {

  private transient Set<String> cdataFields = new HashSet<String>();

  public PrettyStaxDriver() {
    this(new HashSet<String>());
  }

  public PrettyStaxDriver(Set<String> pCdataFields) {
    super();
    this.cdataFields = pCdataFields;
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
}
