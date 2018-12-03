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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.adaptris.util.stream.StreamUtil;

/**
 * Allows you to iterate over a multipart returning each part as a byte array.
 * 
 */
public class ByteArrayIterator extends MultipartIterator implements Iterator<byte[]> {

  private List<BytePartHolder> bodyParts;
  private Iterator<BytePartHolder> bodyPartIterator;

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
  public ByteArrayIterator(InputStream in) throws IOException, MessagingException {
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
  public ByteArrayIterator(byte[] bytes) throws IOException, MessagingException {
    super(bytes);
  }


  /**
   * Get a part by the contentId.
   * <p>
   * Although it is unlikely that the Content-Id will re-occur across a mime multi-part, this is possible, so use of this method may
   * not return the expected body part.
   * </p>
   *
   * @param id the defining content-id.
   * @return the contents of the body part specified by the contentId or null if the contentId is not present.
   */
  public byte[] getPart(String id) {
    byte[] result = null;
    BytePartHolder tmp = new BytePartHolder(id);
    if (bodyParts.contains(tmp)) {
      result = ((BytePartHolder) bodyParts.get(bodyParts.indexOf(tmp))).getBytes();
    }
    return result;
  }

  /**
   * Get a BodyPart based on the it's position within the multipart.
   *
   * @param partNumber the part position (starts from 0).
   * @return the underlying bytes specified by the partNumber.
   */
  public byte[] getPart(int partNumber) {
    if (bodyParts.size() < partNumber + 1) {
      return null;
    }
    return ((BytePartHolder) bodyParts.get(partNumber)).getBytes();
  }

  @Override
  public boolean hasNext() {
    return bodyPartIterator.hasNext();
  }

  @Override
  public byte[] next() {
    return ((BytePartHolder) bodyPartIterator.next()).getBytes();
  }

  @Override
  public void close() {
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
    bodyParts = new Vector<BytePartHolder>();
    MimeMultipart multipart = new MimeMultipart(dataSource);
    for (int i = 0; i < multipart.getCount(); i++) {
      MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(i);
      BytePartHolder ph = new BytePartHolder(part);
      if (bodyParts.contains(ph)) {
        log.warn("{} already exists as a part", ph.contentId);
      }
      bodyParts.add(ph);
    }
    bodyPartIterator = bodyParts.iterator();
  }

  private class BytePartHolder extends KeyedByContentId {
    private byte[] bytes;

    BytePartHolder(String id) {
      super(id);
    }

    BytePartHolder(MimeBodyPart p) throws IOException, MessagingException {
      super(p.getContentID());
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      StreamUtil.copyAndClose(p.getInputStream(), out);
      bytes = out.toByteArray();
    }

    byte[] getBytes() {
      return bytes;
    }
  }
}
