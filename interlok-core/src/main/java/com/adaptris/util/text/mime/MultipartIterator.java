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
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.IdGenerator;

public abstract class MultipartIterator implements Closeable {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());
  protected DataSource dataSource;
  protected static final IdGenerator idGenerator = new GuidGenerator();

  /**
   * Constructor.
   *
   * @param in the Inputstream from which to parse the mime multi-part
   * @throws MessagingException if the bytes did not contain a valid MimeMultiPart
   * @throws IOException if there was an IOException
   * @throws MessagingException if an underlying javax.mail exception occurred
   */
  public MultipartIterator(InputStream in) throws IOException, MessagingException {
    this(new InputStreamDataSource(in));
  }

  /**
   * Constructor.
   *
   * @param bytes the byte array where the mime multi-part is.
   * @throws MessagingException if the bytes did not contain a valid MimeMultiPart
   * @throws IOException if there was an IOException
   * @throws MessagingException if the bytes did not contain a valid MimeMultiPart
   */
  public MultipartIterator(byte[] bytes) throws IOException, MessagingException {
    this(new ByteArrayInputStream(bytes));
  }

  /**
   * Constructor.
   *
   * @param ds the Datasource from which to parse the mime multipart.
   * @throws MessagingException if the bytes did not contain a valid MimeMultiPart
   * @throws IOException if there was an IOException
   */
  protected MultipartIterator(DataSource ds) throws IOException, MessagingException {
    dataSource = ds;
    initIterator();
  }

  /**
   * Throws a {@link UnsupportedOperationException} to add default behaviour in java 7 cases.
   * 
   */
  public void remove() {
    throw new UnsupportedOperationException("Remove is not supported");
  }

  /**
   * Convenience Method to get the content-Type
   *
   */
  public String getContentType() {
    return dataSource.getContentType();
  }

  /**
   * Convenience Method to get the Message-ID from the underlying datasource.
   *
   */
  public String getMessageID() {
    return dataSource.getName();
  }

  public InternetHeaders getHeaders() {
    if (dataSource instanceof MimeHeaders) {
      return ((MimeHeaders) dataSource).getHeaders();
    }
    return new InternetHeaders();
  }

  protected abstract void initIterator() throws MessagingException, IOException;

  protected abstract class KeyedByContentId {

    protected String contentId;

    KeyedByContentId(String s) {
      this.contentId = s;
      if (StringUtils.isEmpty(contentId)) {
        log.warn("No Content Id Found as part of body part, assigning a unique id for referential integrity");
        contentId = idGenerator.create(this);
      }
    }

    @Override
    public boolean equals(Object o) {
      boolean rc = false;
      if (o instanceof KeyedByContentId) {
        rc = contentId.equals(((KeyedByContentId) o).contentId);
      }
      return rc;
    }

    @Override
    public int hashCode() {
      return contentId.hashCode();
    }
  }

}
