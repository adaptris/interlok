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

  public Target() {
    this.target = new StreamResult();
  }


  public Target(Writer charStream) {
    this();
    _setCharStream(charStream);
  }

  public Writer getWriter() throws IOException {
    return new BufferedWriter(target.getWriter());
  }

  private void _setCharStream(Writer charStream) {
    this.target.setWriter(charStream);
  }

} // class Target
