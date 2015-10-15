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

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;



/**
 * Filter on a Custom Header
 * 
 * @author lchan
 * @author $Author: lchan $
 */
class CustomHeaderFilter extends MessageFilterImp {

  private String customHeader;

  CustomHeaderFilter(PatternMatcher m, Pattern p, String hdr) {
    super(m, p);
    customHeader = hdr;
  }

  /**
   * 
   * @see com.adaptris.mail.MessageFilter#accept(javax.mail.Message)
   */
  public boolean accept(Message m) throws MessagingException {
    boolean rc = true;
    String[] s = null;
    if (customHeader != null) {
      rc = false;
      try {
        s = m.getHeader(customHeader);
        // If s is null, then that means we have no headers of
        // of this type, in this instance,
        // we don't have a match...
        if (s != null) {
          for (int i = 0; i < s.length; i++) {
            if (matcher.contains(s[i], pattern)) {
              rc = true;
              break;
            }
          }
        }
      }
      catch (Exception e) {
        ;
      }
    }
    return rc;
  }
}
