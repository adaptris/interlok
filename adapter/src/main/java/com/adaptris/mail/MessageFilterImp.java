/*
 * $RCSfile: MessageFilterImp.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/10/23 14:37:13 $
 * $Author: lchan $
 */
package com.adaptris.mail;

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;



abstract class MessageFilterImp implements MessageFilter {
  protected PatternMatcher matcher;
  protected Pattern pattern;
  
  private MessageFilterImp() {
    
  }
  
  
  MessageFilterImp(PatternMatcher m, Pattern p) {
    this();
    matcher = m;
    pattern = p;
  }  
  
}
