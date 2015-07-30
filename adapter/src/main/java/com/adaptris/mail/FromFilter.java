/*
 * $RCSfile: FromFilter.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/10/23 14:37:13 $
 * $Author: lchan $
 */
package com.adaptris.mail;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;



/**
 * Filter on the From addresses
 * 
 * @author lchan
 * @author $Author: lchan $
 */
class FromFilter extends MessageFilterImp {

  FromFilter(PatternMatcher m, Pattern p) {
    super(m, p);
  }

  /**
   * 
   * @see com.adaptris.mail.MessageFilter#accept(javax.mail.Message)
   */
  public boolean accept(Message m) throws MessagingException {
    Address[] from = m.getFrom();
    boolean rc = false;
    if (pattern != null) {
      if (from != null) {
        for (int j = 0; j < from.length; j++) {
          if (matcher.contains(from[j].toString(), pattern)) {
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
