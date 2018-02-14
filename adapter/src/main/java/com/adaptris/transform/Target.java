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

package com.adaptris.transform;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.transform.stream.StreamResult;
/**
 * Create an instance of this class to provide the transformation
 * method a container for the result of a transformation.
 *
 * <p>When using a set method to set the internal state of a
 * <code>Target</code> object, the most recent set method will unset
 * the effect of any previous set invocation. In other words repeated
 * set method invocations are not cumulative in their effect.</p>
 *
 * @author   Trevor Vaughan
 * @version  0.1 April 2001
 */
public class Target {

  // //////////////////////////////////////
  // internal state
  // //////////////////////////////////////
  private StreamResult target;

  // //////////////////////////////////////
  // constructors
  // //////////////////////////////////////

  /**
   * <p>Zero-argument default constructor. Before you can use
   * a <code>Target</code> in a transformation, you must define
   * the output container by setting its one of its properties.</p>
   *
   * @see #setFileName(String)
   * @see #setCharStream(Writer)
   * @see #setByteStream(OutputStream)
   */
  public Target() {
    this.target = new StreamResult();
  }


  /**
   * <p>
   * Creates a new <code>Target</code> using the character stream. For example...
   * </p>
   * 
   * <pre>
   * {@code 
   *  StringWriter stringWriter = new StringWriter();
   *
   *  Target t = new Target(stringWriter);
   *
   *         (pass Target reference for processing...)
   *
   *  System.out.println("The result of the transformation is...");
   *  System.out.println(stringWriter.toString());
   *  }
   * </pre>
   * 
   * @param charStream the output character stream to wrap.
   */
  public Target(Writer charStream) {
    this();
    _setCharStream(charStream);
  }

  /**
   * <p>Returns a reference to the object's internal state as a
   * <code>Writer</code>.</p>
   *
   * @throws IOException when an error is detected creating the <code>Writer
   * </code>.
   * @return the writer.
   */
  public Writer getWriter() throws IOException {
    return new BufferedWriter(target.getWriter());
  }

  private void _setCharStream(Writer charStream) {
    this.target.setWriter(charStream);
  }

} // class Target
