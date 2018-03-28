/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.mail.attachment;

import static org.apache.commons.lang.StringUtils.defaultIfBlank;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.ContentDisposition;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.MimeHelper;
import com.adaptris.mail.MailException;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.IdGenerator;
import com.adaptris.util.stream.StreamUtil;
import com.adaptris.util.text.mime.BodyPartIterator;
import com.adaptris.util.text.mime.MimeConstants;
import com.adaptris.util.text.mime.PartSelector;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * MailCreator implementation that assumes the AdaptrisMessage is a MIME Multipart.
 * <p>
 * The selected {@link PartSelector} will be used to extract the mime part that will form the body of the email message, the other
 * parts will be added as attachments. It will attempt to interrogate the Content-Disposition and/or Content-Type header of each
 * MIME part to find the correct filename to use for each attachment; if the headers could not be used to infer the correct filename
 * a unique ID is generated for each attachment and that is used as the name.
 * </p>
 * 
 * @config mail-mime-content-creator
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("mail-mime-content-creator")
public class MimeMailCreator implements MailContentCreator {

  private PartSelector bodySelector;
  private transient IdGenerator idGenerator = null;

  private transient Logger logR = LoggerFactory.getLogger(this.getClass());

  public MimeMailCreator() {
    idGenerator = new GuidGenerator();
  }

  /**
   * @see MailContentCreator#createAttachments(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public List<MailAttachment> createAttachments(AdaptrisMessage msg)
      throws MailException {
    if (bodySelector == null) {
      throw new MailException("No way of selecting the body");
    }
    List<MailAttachment> attachments = new ArrayList<MailAttachment>();
    try {
      BodyPartIterator mp = MimeHelper.createBodyPartIterator(msg);
      MimeBodyPart body = bodySelector.select(mp);
      for (int i = 0; i < mp.size(); i++) {
        MimeBodyPart attachment = mp.getBodyPart(i);
        if (!attachment.equals(body)) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          StreamUtil.copyStream(attachment.getInputStream(), out);
          out.flush();
          attachments.add(new MailAttachment(out.toByteArray(),
              getAttachmentFileName(attachment), getContentType(attachment))
                  .withContentTransferEncoding(getContentTransferEncoding(attachment)));
        }
      }
    }
    catch (Exception e) {
      throw new MailException(e);
    }
    return attachments;
  }

  /**
   * @see com.adaptris.core.mail.attachment.MailContentCreator#createBody(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public MailContent createBody(AdaptrisMessage msg) throws MailException {
    if (bodySelector == null) {
      throw new MailException("No way of selecting the body");
    }
    MailContent result = null;
    try {
      BodyPartIterator mp = MimeHelper.createBodyPartIterator(msg);
      MimeBodyPart part = bodySelector.select(mp);
      if (part == null) {
        throw new CoreException("No part selected as the body");
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      StreamUtil.copyStream(part.getInputStream(), out);
      out.flush();
      result = new MailContent(out.toByteArray(), getContentType(part));
    }
    catch (Exception e) {
      throw new MailException(e);
    }
    return result;
  }

  private static ContentType getContentType(MimeBodyPart p) throws Exception {
    ContentType result = null;
    String[] hdr = p.getHeader(MimeConstants.HEADER_CONTENT_TYPE);
    if (hdr != null) {
      result = new ContentType(hdr[0]);
    }
    return result;
  }

  private static String getContentTransferEncoding(MimeBodyPart p) throws Exception {
    String result = null;
    String[] hdr = p.getHeader(MimeConstants.HEADER_CONTENT_ENCODING);
    if (hdr != null) {
      result = hdr[0];
    }
    return defaultIfBlank(result, "base64");
  }

  private String getAttachmentFileName(MimeBodyPart p) throws Exception {
    String filename = null;
    String[] hdr = p.getHeader("Content-Disposition");
    if (hdr != null) {
      ContentDisposition cd = new ContentDisposition(hdr[0]);
      filename = cd.getParameter("filename");
    }
    if (filename == null) {
      hdr = p.getHeader("Content-Type");
      if (hdr != null) {
        ContentType ct = new ContentType(hdr[0]);
        filename = ct.getParameter("name");
      }
    }
    if (filename == null) {
      filename = idGenerator.create(p);
      logR.warn("Could not determine filename for MimeBodyPart, assigning unique filename of {}", filename);

    }
    return filename;
  }

  /**
   * @return the bodySelector
   */
  public PartSelector getBodySelector() {
    return bodySelector;
  }

  /**
   * @param ps the bodySelector to set
   */
  public void setBodySelector(PartSelector ps) {
    bodySelector = ps;
  }
}
