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
