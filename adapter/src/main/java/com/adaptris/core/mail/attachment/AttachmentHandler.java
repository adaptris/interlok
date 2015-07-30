package com.adaptris.core.mail.attachment;

import java.util.List;

import org.w3c.dom.Document;


/**
 * Interface for handling attachments for {@link XmlMailCreator}
 * @author lchan
 * @author $Author: lchan $
 */
public interface AttachmentHandler {

  List<MailAttachment> resolve(Document d) throws Exception;
}
