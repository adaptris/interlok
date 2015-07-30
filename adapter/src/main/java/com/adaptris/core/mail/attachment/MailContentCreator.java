package com.adaptris.core.mail.attachment;

import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.mail.MailException;

/**
 * Interface for creating a mail message from an {@link AdaptrisMessage}
 *
 * @author lchan
 * @author $Author: lchan $
 */
public interface MailContentCreator {

  /**
   * Create a list of attachments.
   *
   * @param msg the source message.
   * @return a list (with size 0 if there are no attachments) of {@link MailAttachment} objects
   * @throws MailException wrapping any other exceptions
   */
  List<MailAttachment> createAttachments(AdaptrisMessage msg)
      throws MailException;

  /**
   * Create the body of the mail message.
   * 
   * @param msg the source message.
   * @return the body of the message.
   * @throws MailException wrapping any other exceptions
   */
  MailContent createBody(AdaptrisMessage msg) throws MailException;
}
