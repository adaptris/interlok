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

import javax.mail.URLName;

import org.apache.commons.net.pop3.POP3Client;

import com.icegreen.greenmail.pop3.Pop3Server;
import com.icegreen.greenmail.util.GreenMail;

public class TestPop3Receive extends MailReceiverCase {

  public TestPop3Receive(String name) {
    super(name);
  }

  protected MailReceiver createClient(GreenMail gm) throws Exception {
    Pop3Server server = gm.getPop3();
    String pop3UrlString = server.getProtocol() + "://localhost:" + server.getPort() + "/INBOX";
    URLName pop3Url = createURLName(pop3UrlString, DEFAULT_POP3_USER, DEFAULT_ENCODED_POP3_PASSWORD);
    ApachePOP3 client = new ApachePOP3(pop3Url, new ApacheClientConfig() {

      @Override
      POP3Client preConnectConfigure(POP3Client client) throws MailException {
        return client;
      }

      @Override
      POP3Client postConnectConfigure(POP3Client client) throws MailException {
        return client;
      }
    });
    return client;
  }

}
