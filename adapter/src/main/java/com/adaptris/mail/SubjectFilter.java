/*
 * $RCSfile: SubjectFilter.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/10/23 14:37:13 $
 * $Author: lchan $
 */
package com.adaptris.mail;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;


/**
 * Filter on the Subject.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
class SubjectFilter extends MessageFilterImp {

  SubjectFilter(PatternMatcher m, Pattern p) {
    super(m, p);
  }

  /**
   * 
   * @see com.adaptris.mail.MessageFilter#accept(javax.mail.Message)
   */
  public boolean accept(Message m) throws MessagingException {
    boolean rc = false;
    if (pattern != null) {
      if (matcher.contains(m.getSubject(), pattern)) {
        rc = true;
      }
    }
    else {
      rc = true;
    }
    return rc;
  }
}
