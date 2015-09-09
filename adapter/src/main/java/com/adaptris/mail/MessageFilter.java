package com.adaptris.mail;

import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * Interface for filtering mailboxes.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public interface MessageFilter {

  /**
   * Whether or not to accept the message.
   * 
   * @param m the message
   * @return true if the message matches the filter.
   */
  boolean accept(Message m) throws MessagingException;
}
