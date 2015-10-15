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

import org.apache.commons.net.pop3.POP3Client;


/**
 * Allows configuration of additional settings on the POP3 Client.
 * 
 * @author lchan
 * 
 */
abstract class ApacheClientConfig {

  /**
   * Configure things like Connection Timeout, Cipher Suites etc.
   * 
   */
  abstract POP3Client preConnectConfigure(POP3Client client) throws MailException;

  /**
   * Configure things like TCPNODELAY and SO_KEEPALIVE that only work when you have a socket open.
   * 
   */
  abstract POP3Client postConnectConfigure(POP3Client client) throws MailException;

}
