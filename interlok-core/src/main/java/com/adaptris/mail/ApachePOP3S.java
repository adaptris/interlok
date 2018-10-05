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

package com.adaptris.mail;

import java.io.IOException;

import javax.mail.URLName;
import javax.net.ssl.SSLContext;

import org.apache.commons.net.pop3.POP3SClient;

/**
 * Mailbox client implementation using commons net pop3.
 * 
 * POP3Client can handle POP3; but only if you are in explicit SSL mode; in that instance you have to call execTLS() to start the
 * TLS negotiation. So, if you never call it, then you never negotiate TLS, which means you're just using plain POP3.
 * 
 * It's probably too hard to figure out based on the URLName exactly what we need to do; so let's make it a separate impl to keep
 * things quite clear.
 * 
 * @author lchan
 * 
 */
class ApachePOP3S extends ApacheMailClient<POP3SClient> {

  private transient boolean implicitSSL = false;
  private transient SSLContext sslContext = null;

  ApachePOP3S(URLName url, ApacheClientConfig clientConfig, boolean implicit) {
    super(url, clientConfig);
    this.implicitSSL = implicit;
  }

  // This is really a constructor for tests.
  ApachePOP3S(URLName url, ApacheClientConfig clientConfig, boolean implicit, SSLContext context) {
    this(url, clientConfig, implicit);
    this.sslContext = context;
  }


  @Override
  POP3SClient createClient() throws MailException {
    return new POP3SClient(implicitSSL, sslContext);
  }

  @Override
  void postConnectAction(POP3SClient client) throws MailException, IOException {
    if (!implicitSSL && !client.execTLS()) {
      throw new MailException("Could not negotiate TLS after connect.");
    }
  }
  
}
