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

package com.adaptris.util.text.mime;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.util.GuidGenerator;
import com.adaptris.util.IdGenerator;

public class MultiPartFileInput implements Iterator<MimeBodyPart>, Closeable {

  private List<PartHolder> bodyParts;
  private Iterator<PartHolder> bodyPartIterator;
  private MimeMultipart multipart;
  private transient Logger log = LoggerFactory.getLogger(this.getClass());
  private transient ReadonlyFileDataSource dataSource;

  private static IdGenerator idGenerator;

  static {
    idGenerator = new GuidGenerator();
  }

  private MultiPartFileInput(ReadonlyFileDataSource in) throws IOException, MessagingException {
    bodyParts = new Vector<PartHolder>();
    initialise(in);
  }

  public MultiPartFileInput(File in) throws IOException, MessagingException {
    this(new ReadonlyFileDataSource(in));
  }


  public void close() throws IOException {
    dataSource.close();
  }

  public MimeBodyPart getBodyPart(String id) {
    MimeBodyPart result = null;
    PartHolder tmp = wrap(id);

    if (bodyParts.contains(tmp)) {
      result = ((PartHolder) bodyParts.get(bodyParts.indexOf(tmp))).getPart();
    }
    return result;
  }

  public MimeBodyPart getBodyPart(int partNumber) {
    if (bodyParts.size() < partNumber + 1) {
      return null;
    }
    return ((PartHolder) bodyParts.get(partNumber)).getPart();
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
  public void remove() {
    throw new UnsupportedOperationException("Remove is not supported");
  }

  /**
   * Return the number of body parts in this mime multipart.
   *
   * @return the number of body parts
   */
  public int size() {
    return bodyParts.size();
  }

  public String getMessageID() {
    return dataSource.getName();
  }

  private void initialise(ReadonlyFileDataSource in) throws MessagingException, IOException {
    dataSource = in;
    multipart = new MimeMultipart(dataSource);
    for (int i = 0; i < multipart.getCount(); i++) {
      MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(i);
      PartHolder ph = wrap(part);
      if (bodyParts.contains(ph)) {
        log.warn("{} already exists as a part", ph.contentId);
      }
      bodyParts.add(ph);
    }
    bodyPartIterator = bodyParts.iterator();
  }

  private PartHolder wrap(MimeBodyPart part) throws MessagingException, IOException {
    return new PartHolder(part);
  }

  private PartHolder wrap(String id) {
    return new PartHolder(id);
  }

  private class PartHolder {
    private MimeBodyPart bodyPart;
    private String contentId;

    PartHolder(String id) {
      contentId = id;
      bodyPart = null;
    }

    PartHolder(MimeBodyPart p) throws IOException, MessagingException {
      bodyPart = p;
      contentId = bodyPart.getContentID();
      if (contentId == null) {
        log.warn("No Content Id Found as part of body part, assigning a unique id for referential integrity");
        contentId = idGenerator.create(p);
      }
    }

    MimeBodyPart getPart() {
      return bodyPart;
    }

    @Override
    public boolean equals(Object o) {
      boolean rc = false;
      if (o instanceof PartHolder) {
        rc = contentId.equals(((PartHolder) o).contentId);
      }
      return rc;
    }

    @Override
    public int hashCode() {
      return contentId.hashCode();
    }
  }
}
