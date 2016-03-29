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

package com.adaptris.http.legacy;

import com.adaptris.core.NoOpConnection;
import com.adaptris.http.HttpClient;
import com.adaptris.http.HttpClientTransport;
import com.adaptris.http.HttpException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A Produce Connection for HTTP.
 * <p>
 * This extends <code>NullConnection</code> intentionally
 * <p>
 * <strong>Requires an HTTP license</strong>
 * </p>
 * 
* <p>
 * In the adapter configuration file this class is aliased as <b>http-produce-connection</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 
 * @author lchan
 * @author $Author: lchan $
 * @deprecated since 2.9.0 use {@link com.adaptris.core.http.JdkHttpProducer} instead
 */
@Deprecated
@XStreamAlias("http-produce-connection")
public class HttpProduceConnection extends NoOpConnection implements
    HttpClientConnection {

  /**
   * @see HttpClientConnection#initialiseClient(java.lang.String)
   */
  @Override
  public HttpClientTransport initialiseClient(String url) throws HttpException {
    return new HttpClient(url);
  }

}
