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

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;

/**
 * Stream writer that outputs Beautified JSON.
 * <p>
 * The super class generates a single JSON object that contains all data, this is accessed when the writeEndDocument event is
 * processed. A Vistor then navigates this object and generates a formated string from it.
 * </p>
 * 
 * @author bklair
 */
public class PrettyMappedJSONStreamWriter extends MappedXMLStreamWriter {

  protected int indentSize;
  protected char indentChar;
  protected JsonVisitor visitor = new JsonVisitor(4, ' ');

  public PrettyMappedJSONStreamWriter(MappedNamespaceConvention convention, Writer writer) {
    this(convention, writer, 4, ' ');
  }

  public PrettyMappedJSONStreamWriter(MappedNamespaceConvention convention, Writer writer, int indentSize, char indentChar) {
    super(convention, writer);
    this.indentSize = indentSize;
    this.indentChar = indentChar;
    visitor = new JsonVisitor(indentSize, indentChar);
  }

  /**
   * Outputs a formatted value to the writer prefixed with the appropriate indent
   */
  @Override
  protected void writeJSONObject(JSONObject root) throws XMLStreamException {
    try {
      visitor.visit(root, 0);
    } catch (JSONException e1) {
      throw new XMLStreamException(e1);
    }

    try {
      writer.write(visitor.toString());
    } catch (IOException e) {
      throw new XMLStreamException(e);
    }
  }

  /**
   * Vistor Class that processes the fully built up JSON object and generates a formated string representation from it.
   * 
   * @author bklair
   */
  private static class JsonVisitor {

    private final StringBuilder builder = new StringBuilder();
    private final int indentationSize;
    private final char indentationChar;

    public JsonVisitor(final int indentationSize, final char indentationChar){
      this.indentationSize = indentationSize;
      this.indentationChar = indentationChar;
    }

    private void visit(final JSONArray array, final int indent) throws JSONException{
      final int length = array.length();
      if (length == 0){
        write("[]", indent);
      } else{
        write("[", indent);
        for(int i = 0; i < length; i++){
          visit(array.get(i), indent + 1);
        }
        write("]", indent);
      }
    }

    private void visit(final JSONObject obj, final int indent) throws JSONException{
      final int length = obj.length();
      if(length == 0){
        write("{}", indent);
      } else{
        write("{", indent);
        @SuppressWarnings("unchecked")
        final Iterator<String> keys = obj.keys();
        while(keys.hasNext()){
          final String key = keys.next();
          final Object value = obj.get(key);
          if (isSingleValueClass(value.getClass())) {
            String commaString = keys.hasNext() ? "," : ""; 
            if (CharSequence.class.isAssignableFrom(value.getClass())) {
              write("\"" + key + "\" : \"" + value + "\"" +commaString, indent + 1);
            }
            else 
              write("\"" + key + "\" : " + value + commaString, indent + 1);
          }
          else {
            write("\"" + key + "\" :", indent + 1);
            visit(obj.get(key), indent + 1);
            if (keys.hasNext()){
              write(",", indent + 1);
            }
          }
        }
        write("}", indent);
      }
    }

    /**
     * Determine if the given class will translate to a single value in the output eg Strings, primitives etc.
     * Class.isPrimitive does not return true for java.lang.Long, hence the need for this method!
     * @param clazz
     * @return false unless the given class is a string or primitive based type 
     */
    private boolean isSingleValueClass(Class<?> clazz) {
      if (CharSequence.class.isAssignableFrom(clazz))
        return true;
      else if (clazz.isPrimitive())
        return true;
      else if (Number.class.isAssignableFrom(clazz))
        return true;
      else if (Boolean.class.isAssignableFrom(clazz))
        return true;

      return false;
    }

    private void visit(final Object object, final int indent) throws JSONException{
      if(object instanceof JSONArray){
        visit((JSONArray) object, indent);
      } else if(object instanceof JSONObject){
        visit((JSONObject) object, indent);
      } else{
        if(object instanceof String){
          write("\"" + (String) object + "\"", indent);
        } else{
          write(String.valueOf(object), indent);
        }
      }
    }

    private void write(final String data, final int indent) {
      if (indent > 0) {
        char[] indentValue = new char[indent * indentationSize];
        Arrays.fill(indentValue, indentationChar);
        builder.append(indentValue);
      }
      //        for(int i = 0; i < (indent * indentationSize); i++) {
      //            builder.append(indentationChar);
      //        }
      builder.append(data).append('\n');
    }

    @Override
    public String toString(){
      return builder.toString();
    }
  }
}
