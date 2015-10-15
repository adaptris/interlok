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

import com.icegreen.greenmail.pop3.Pop3Server;
import com.icegreen.greenmail.util.GreenMail;

public class TestJavamailPop3Receive extends MailReceiverCase {

  public TestJavamailPop3Receive(String name) {
    super(name);
  }

  protected MailReceiver createClient(GreenMail gm) throws Exception {
    Pop3Server server = gm.getPop3();
    String pop3Url = server.getProtocol() + "://localhost:" + server.getPort() + "/INBOX";
    MailboxClient client = new MailboxClient(createURLName(pop3Url, DEFAULT_POP3_USER, DEFAULT_ENCODED_POP3_PASSWORD));
    return client;
  }


}
