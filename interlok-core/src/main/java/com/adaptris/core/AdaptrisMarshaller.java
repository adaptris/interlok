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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import com.adaptris.util.URLString;

/**
 * <p>
 * Defines methods required to 'marshal' Java objects to XML.
 * </p>
 */
public interface AdaptrisMarshaller {

  /**
   * Marshalls an object to XML.
   *
   * @param obj the <code>Object</code> to marshall to XML
   * @return a XML representation of the <code>Object</code>
   * @throws CoreException wrapping any underlying <code>Exception</code>
   */
  String marshal(Object obj) throws CoreException;

  /**
   * Marshalls an object to XML.
   *
   * @param obj the <code>Object</code> to marshall to XML
   * @param fileName the name of the file to write to
   * @throws CoreException wrapping any underlying <code>Exception</code>
   */
  void marshal(Object obj, String fileName) throws CoreException;

  /**
   * Marshalls an object to XML.
   *
   * @param obj the <code>Object</code> to marshall to XML
   * @param file the file to write to
   * @throws CoreException wrapping any underlying <code>Exception</code>
   */
  void marshal(Object obj, File file) throws CoreException;

  /**
   * Marshalls an object to XML.
   *
   *
   * @param obj the <code>Object</code> to marshall to XML
   * @param writer the writer to write to
   * @throws CoreException wrapping any underlying <code>Exception</code>
   */
  void marshal(Object obj, Writer writer) throws CoreException;

  /**
   * Marshalls an object to XML.
   *
   *
   * @param obj the object to marshall to XML
   * @param outputStream the OutputStream to write to
   * @throws CoreException wrapping any underlying <code>Exception</code>
   */
  void marshal(Object obj, OutputStream outputStream) throws CoreException;

  /**
   * <p>
   * Marshalls an XML representation of the passed <code>Object</code> to the
   * file sytem location denoted by the passed <code>URL</code>.
   * </p>
   * @param obj the <code>Object</code> to marshall to XML
   * @param fileUrl the file system location to write to
   * @throws CoreException wrapping any underlying <code>Exception</code>
   */
  void marshal(Object obj, URL fileUrl) throws CoreException;

  /**
   * <p>
   * Unmarshalls an <code>Object</code> from the passed XML.
   * </p>
   * @param xml the <code>String</code> to unmarshal
   * @return an <code>Object</code>
   * @throws CoreException wrapping any underlying <code>Exception</code>
   */
  Object unmarshal(String xml) throws CoreException;

  /**
   * <p>
   * Unmarshalls an <code>Object</code> based on the passed <code>file</code>.
   * </p>
   * @param file a file containing XML to unmarshal
   * @return an <code>Object</code>
   * @throws CoreException wrapping any underlying <code>Exception</code>
   */
  Object unmarshal(File file) throws CoreException;

  /**
   * <p>
   * Unmarshalls an <code>Object</code> based on the passed file system
   * <code>URL</code>.
   * </p>
   * @param fileUrl the file system location to read from
   * @return an <code>Object</code>
   * @throws CoreException wrapping any underlying <code>Exception</code>
   */
  Object unmarshal(URL fileUrl) throws CoreException;

  /**
   * <p>
   * Unmarshalls an <code>Object</code> based on the passed <code>Reader</code>.
   * </p>
   * @param reader a <code>Reader</code> with XML to unmarshal
   * @return an <code>Object</code>
   * @throws CoreException wrapping any underlying <code>Exception</code>
   */
  Object unmarshal(Reader reader) throws CoreException;

  /**
   * <p>
   * Unmarshals an <code>Object</code> based on the passed
   * <code>InputStream</code>.
   * </p>
   * @param stream an <code>InputStream</code> of XML to unmarshal
   * @return an <code>Object</code>
   * @throws CoreException wrapping any underlying <code>Exception</code>
   */
  Object unmarshal(InputStream stream) throws CoreException;

  /**
   * <p>
   * Unmarshals an <code>Object</code> from the passed <code>URLString</code>
   * location.
   * </p>
   * @param location the location to unmarshal from
   * @return the unmarshalled <code>Object</code>
   * @throws CoreException wrapping any underlying <code>Exception</code>s
   */
  Object unmarshal(URLString location) throws CoreException;
}
