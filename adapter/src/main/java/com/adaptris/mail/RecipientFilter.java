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

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;



/**
 * Filter on the Recipients
 * 
 * @author lchan
 * @author $Author: lchan $
 */
class RecipientFilter extends MessageFilterImp {

  RecipientFilter(PatternMatcher m, Pattern p) {
    super(m, p);
  }

  /**
   * 
   * @see com.adaptris.mail.MessageFilter#accept(javax.mail.Message)
   */
  public boolean accept(Message m) throws MessagingException {
    Address[] recipients = m.getAllRecipients();
    boolean rc = false;
    if (pattern != null) {
      if (recipients != null) {
        for (int j = 0; j < recipients.length; j++) {
          if (matcher.contains(recipients[j].toString(), pattern)) {
            rc = true;
            break;
          }
        }
      }
    }
    else {
      rc = true;
    }
    return rc;
  }
}
