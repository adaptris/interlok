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
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdapterXStreamMarshallerFactory;

/**
 * General Utilities used by various XStream related classes
 * 
 * @author bklair
 */
public abstract class XStreamUtils {
	
	private static Logger log = LoggerFactory.getLogger(XStreamUtils.class);
	
	/**
	 * Converts a lowercase hyphen separated format into a camelcase based
	 * format. Used by the unmarshalling process to convert an xml element into
	 * a java class/field name.
	 * 
	 * @param xmlElementName
	 *            - Current element name to be processed.
	 * @return translated name
	 */
  public static String toFieldName(String xmlElementName) {
    if (xmlElementName == null) {
      return null;
    }
    if (xmlElementName.length() == 0) {
      return xmlElementName;
    }
    if (xmlElementName.length() == 1) {
      return xmlElementName.toLowerCase();
    }
    // -- Follow the Java beans Introspector::decapitalize
    // -- convention by leaving alone String that start with
    // -- 2 uppercase characters.
    if (Character.isUpperCase(xmlElementName.charAt(0))
        && Character.isUpperCase(xmlElementName.charAt(1))) {
      return xmlElementName;
    }
    // -- process each character
    StringBuilder input = new StringBuilder(xmlElementName);
    StringBuilder output = new StringBuilder();
    output.append(Character.toLowerCase(input.charAt(0)));
    boolean multiHyphens = false;
    for (int i = 1; i < input.length(); i++) {
      char ch = input.charAt(i);
      if (ch == '-') {
        if (input.charAt(++i) != '-') {
          output.append(Character.toUpperCase(input.charAt(i)));
        } else {
          multiHyphens = true;
        }
      } else {
        if (multiHyphens) {
          output.append(Character.toUpperCase(ch));
        } else {
          output.append(ch);
        }
        multiHyphens = false;

      }
    }
    return output.toString();
  }
    
    /**
   * Converts a camelcase name into a lowercase hyphen separated format for output to XML. Used by the marshalling process to
   * convert a java class/field name into an xml element name.
   * 
   * @param fieldName - Current element name to be processed.
   * @return translated name
   */
  public static String toXmlElementName(String fieldName) {
    if (fieldName == null) {
      return null;
    }
    if (fieldName.length() == 0) {
      return fieldName;
    }
    if (fieldName.length() == 1) {
      return fieldName.toLowerCase();
    }

    // -- Follow the Java beans Introspector::decapitalize
    // -- convention by leaving alone String that start with
    // -- 2 uppercase characters.
    if (Character.isUpperCase(fieldName.charAt(0))
        && Character.isUpperCase(fieldName.charAt(1))) {
      return fieldName;
    }

    // -- process each character
    StringBuilder cbuff = new StringBuilder(fieldName);
    cbuff.setCharAt(0, Character.toLowerCase(cbuff.charAt(0)));

    boolean ucPrev = false;
    for (int i = 1; i < cbuff.length(); i++) {
      char ch = cbuff.charAt(i);
      if (Character.isUpperCase(ch)) {
        if (ucPrev) {
          continue;
        }
        ucPrev = true;
        cbuff.insert(i, '-');
        ++i;
        cbuff.setCharAt(i, Character.toLowerCase(ch));
      } else {
        ucPrev = false;
      }
    }
    return cbuff.toString();
  }
	
    /**
	 * Given a Field of a Class this method will return a Set of a number of
	 * possible fully qualified reference names for the field. This would be
	 * based on the class hierarchy eg currentClass:field, parentClass:field,
	 * grandparentClass:field etc.
	 * 
	 * @param clazz - Parent Class of the given field
	 * @param field - Given field to process
	 * @param separator - class-field separator
	 * @return - Set<String> of possible paths for the field
	 */
  public static Collection<String> createParentFields(Class<?> clazz, String field, String separator) {
    Set<String> result = new HashSet<String>();
    result.add(clazz.getCanonicalName() + separator + field);
    Class<?> c = clazz;
    while (c.getSuperclass() != null) {
      c = c.getSuperclass();
      result.add(c.getCanonicalName() + separator + field);
    }
    return result;
  }
	
  /**
   * Determine if the given Set <code>toCheck</code> contains any of the elements within <code>possibles</code>
   * 
   * @param toCheck - Set of Strings to check
   * @param possibles - Collection of
   * @return true if the set contains the possibles.
   */
  public static boolean setContainsAnyOf(Set<String> toCheck, Collection<String> possibles) {
    Set<String> copy = new HashSet<String>(toCheck);
    copy.retainAll(possibles);
    return copy.size() > 0;
  }
    
  /**
   * Reads in the entire file contents skipping any blank lines.
   * 
   * @param in - InputStream to read
   * @return List<String> List of lines.
   * @throws IOException
   */
  public static List<String> readResource(InputStream in) throws IOException {
    List<String> result = new ArrayList<String>();
    try (InputStream closeable = in) {
      result = IOUtils.readLines(closeable, Charset.defaultCharset());
      // Well, is this more or less readable the other it's not even as
      // though the predicate style actually returns a new list.
      // result.removeIf(StringUtils::isBlank);
      result.removeAll(Arrays.asList("", null));
    }
    return result;
  }
	
  /**
   * Reads in the given resource file and converts each line of input into a
   * Class.
   * 
   * @param in - Opened input stream to resource file
   * @return List of Classes
   * @throws IOException
   */
  public static List<Class<?>> getClasses(InputStream in) throws IOException {
    List<Class<?>> result = new ArrayList<Class<?>>();
    List<String> lines = readResource(in);
    for (String clazz : lines) {
      try {
        result.add(Class.forName(clazz));
      } catch (ClassNotFoundException | NoClassDefFoundError e) {
        traceLogging("Ignoring missing class [{}] :{}", e.getClass().getSimpleName(), e.getMessage());
      }
    }
    return result;
  }
  
  private static void traceLogging(String format, Object... objs) {
    if (AdapterXStreamMarshallerFactory.XSTREAM_DBG) {
      log.trace(format, objs);
    }
  }
}
