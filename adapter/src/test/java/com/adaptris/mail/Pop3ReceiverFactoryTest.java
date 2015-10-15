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

public class Pop3ReceiverFactoryTest extends Pop3FactoryCase {

  public Pop3ReceiverFactoryTest(String name) {
    super(name);
  }

  @Override
  Pop3ReceiverFactory create() {
    return new Pop3ReceiverFactory();
  }

  @Override
  Pop3Server getServer(GreenMail gm) {
    return gm.getPop3();
  }

  @Override
  Pop3ReceiverFactory configure(Pop3ReceiverFactory f) {
    f.setConnectTimeout(60000);
    f.setKeepAlive(true);
    f.setReceiveBufferSize(8192);
    f.setSendBufferSize(8192);
    f.setTcpNoDelay(true);
    f.setTimeout(60000);
    return f;
  }

}
