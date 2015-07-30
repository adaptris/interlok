package com.adaptris.util.text.mime;

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;


/**
 * Select a specific MimeBodyPart from a Mime Multipart.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public interface PartSelector {

  /**
   * Select the body part that should form the AdaptrisMessage payload.
   * 
   * @param in a MultiPartInput whose iterator returns a MimeBodyPart.
   * @return the MimeBodyPart that should be the body, or null if no match
   *         found.
   */
  MimeBodyPart select(MultiPartInput in) throws MessagingException;

  /**
   * Select the body part that should form the AdaptrisMessage payload.
   * 
   * @param in	a MimeMultipart
   * @return a list of MimeBodyPart that should be the body, empty if no match
   *         found.
   */
  List<MimeBodyPart> select(MimeMultipart in) throws MessagingException;

}
