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

package com.adaptris.http;

/** The HttpMessageFactory.
 *  <p>This class is used to create HttpMessage instances.
 */
public abstract class HttpMessageFactory {

  private static HttpMessageFactory defaultfactory = null;
  /** Creates a new instance of HttpMessageFactory */
  protected HttpMessageFactory() {
  }

  /** Get the default implementation of the factory.
   *  @return a HttpMessageFactory
   */
  public static final HttpMessageFactory getDefaultInstance() {
    if (defaultfactory == null) {
      defaultfactory = new HttpDefMessageFactory();
    }
    return defaultfactory;
  }

  /** Create a HttpMessage
   *  @return a HttpMessage
   *  @throws HttpException on error.
   */
  public abstract HttpMessage create() throws HttpException;

  private static class HttpDefMessageFactory extends HttpMessageFactory {
    public HttpMessage create() throws HttpException {
      return (new SimpleHttpMsg());
    }
  }
}
