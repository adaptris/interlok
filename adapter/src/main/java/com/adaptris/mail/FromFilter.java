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

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;



/**
 * Filter on the From addresses
 * 
 * @author lchan
 * @author $Author: lchan $
 */
class FromFilter extends MessageFilterImp {

  FromFilter(MatchProxy m) {
    super(m);
  }

  /**
   * 
   * @see com.adaptris.mail.MessageFilter#accept(javax.mail.Message)
   */
  public boolean accept(Message m) throws MessagingException {
    Address[] from = m.getFrom();
    boolean rc = false;
    if (from != null) {
      for (int j = 0; j < from.length; j++) {
        if (matcher.matches(from[j].toString())) {
          rc = true;
          break;
        }
      }
    }
    return rc;
  }
}
