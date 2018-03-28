/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.util.text.mime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

/**
 * Allows you to iterate over a multipart returning each part as a {@code MimeBodyPart}.
 * 
 */
public class BodyPartIterator extends MultipartIterator implements Iterator<MimeBodyPart> {

  private List<MimePartHolder> bodyParts;
  private Iterator<MimePartHolder> bodyPartIterator;


  /**
   * Constructor.
   *
   * @param in the Inputstream from which to parse the mime multi-part
   * @throws MessagingException if the bytes did not contain a valid
   *           MimeMultiPart
   * @throws IOException if there was an IOException
   * @throws MessagingException if an underlying javax.mail exception occurred
   * @see MultiPartInput#MultiPartInput(InputStream, boolean)
   */
  public BodyPartIterator(InputStream in) throws IOException, MessagingException {
    super(in);
  }

  /**
   * Constructor.
   *
   * @param bytes the byte array where the mime multi-part is.
   * @throws MessagingException if the bytes did not contain a valid
   *           MimeMultiPart
   * @throws IOException if there was an IOException
   * @throws MessagingException if the bytes did not contain a valid
   *           MimeMultiPart
   * @see MultiPartInput#MultiPartInput(byte[], boolean)
   */
  public BodyPartIterator(byte[] bytes) throws IOException, MessagingException {
    super(new ByteArrayInputStream(bytes));
  }

  protected BodyPartIterator(DataSource ds) throws IOException, MessagingException {
    super(ds);
  }

  public MimeBodyPart getBodyPart(String id) {
    MimeBodyPart result = null;
    MimePartHolder tmp = new MimePartHolder(id);

    if (bodyParts.contains(tmp)) {
      result = bodyParts.get(bodyParts.indexOf(tmp)).getPart();
    }
    return result;
  }

  public MimeBodyPart getBodyPart(int partNumber) {
    if (bodyParts.size() < partNumber + 1) {
      return null;
    }
    return bodyParts.get(partNumber).getPart();
  }

  @Override
  public boolean hasNext() {
    return bodyPartIterator.hasNext();
  }

  @Override
  public MimeBodyPart next() {
    return bodyPartIterator.next().getPart();
  }

  @Override
  public void close() throws IOException {

  }

  /**
   * Return the number of body parts in this mime multipart.
   *
   * @return the number of body parts
   */
  public int size() {
    return bodyParts.size();
  }

  @Override
  protected void initIterator() throws MessagingException, IOException {
    bodyParts = new Vector<MimePartHolder>();
    MimeMultipart multipart = new MimeMultipart(dataSource);
    for (int i = 0; i < multipart.getCount(); i++) {
      MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(i);
      MimePartHolder ph = new MimePartHolder(part);
      if (bodyParts.contains(ph)) {
        log.warn("{} already exists as a part", ph.contentId);
      }
      bodyParts.add(ph);
    }
    bodyPartIterator = bodyParts.iterator();
  }

  protected class MimePartHolder extends KeyedByContentId {
    private MimeBodyPart bodyPart;

    MimePartHolder(String id) {
      super(id);
    }

    MimePartHolder(MimeBodyPart p) throws IOException, MessagingException {
      super(p.getContentID());
      bodyPart = p;
    }

    MimeBodyPart getPart() {
      return bodyPart;
    }
  }
}
