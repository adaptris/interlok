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

package com.adaptris.http.test;



import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.http.RequestProcessor;



/**

 * @author lchan

 * @author $Author: lchan $

 */

abstract class BaseRequestProcessor implements RequestProcessor {



  private Integer id;

  protected static final byte[] responseBytes =

    "This is just some arbitary data we want to return".getBytes();



  private String uri;

  protected transient Log logR;

  protected Properties config;



  BaseRequestProcessor() {

    logR = LogFactory.getLog(this.getClass());

  }

  

  BaseRequestProcessor(String uri, Integer id, Properties config) {

    this();

    setUri(uri);

    this.id = id;

    this.config = config;

  }



  /** Set the URI.

   *  @param uri the uri.

   *  @see RequestProcessor#getUri()

   */

  public void setUri(String uri) {

    this.uri = uri;

  }



  /** @see RequestProcessor#getUri()

   */

  public String getUri() {

    return uri;

  }

  

  /**

   * 

   * @see java.lang.Object#toString()

   */

  public String toString() {

    String className = this.getClass().getName();

    int dot = className.lastIndexOf(".");

    if (dot > 0) {

      className = className.substring(dot + 1);

    }    

    return "[" + className + id + "]";

  }

}
