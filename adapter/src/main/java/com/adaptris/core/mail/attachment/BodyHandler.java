package com.adaptris.core.mail.attachment;

import org.w3c.dom.Document;



/**
 * Interface for handling the email body for {@link XmlMailCreator}
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public interface BodyHandler {

  MailContent resolve(Document d) throws Exception;
}
