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

package com.adaptris.annotation;




public class AnnotationConstants {

  public static final String PACKAGE_PREFIX = "META-INF/adaptris";

  /**
   * The properties file that forces XStream to use standard javabeans getters and setters.
   *
   */
  public static final String BEAN_INFO_PROPERTIES_FILE = PACKAGE_PREFIX + "/javabeans.properties";
  /**
   * The properties file that contains references to classes that require special CDATA handling.
   *
   */
  public static final String CDATA_PROPERTIES_FILE = PACKAGE_PREFIX + "/cdata.properties";
  /**
   * The properties file that contains references to classes that have XStreamAlias annotations.
   */
  public static final String XSTREAM_ALIAS_PROPERTIES_FILE = PACKAGE_PREFIX + "/XStreamAlias.properties";
  /**
   * The properties file that contains references to classes that have XStreamImplicit annotations.
   *
   */
  public static final String XSTREAM_IMPLICIT_PROPERTIES_FILE = PACKAGE_PREFIX + "/XStreamImplicit.properties";
  /**
   * The properties file that contains references to classes that are Adaptris Services
   *
   */
  public static final String COMPONENT_PROPERTIES_FILE = PACKAGE_PREFIX + "/component.properties";
  /**
   * The standard separator between class names and members.
   *
   */
  public static final String STANDARD_FIELD_SEPARATOR = "#";

}
