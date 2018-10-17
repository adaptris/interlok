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

import org.apache.commons.net.pop3.POP3Client;

/**
 * Mailbox client implementation using commons net pop3.
 * 
 * @author lchan
 * 
 */
class ApachePOP3 extends ApacheMailClient<POP3Client> {

  ApachePOP3(URLName url, ApacheClientConfig clientConfig) {
    super(url, clientConfig);
  }

  @Override
  POP3Client createClient() throws MailException {
    return new POP3Client();
  }

  @Override
  void postConnectAction(POP3Client client) throws MailException, IOException {
  }


}
