package com.adaptris.mail;

import javax.mail.URLName;

/**
 * Interface for creating a mail receiver.
 * 
 * @author lchan
 * 
 */
public interface MailReceiverFactory {

  MailReceiver createClient(URLName url) throws MailException;

}
