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

package com.adaptris.core.services.metadata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Takes a metadata value and sets that as the payload.
 * 
 * <p>This can be treated as a simplified form of {@link PayloadFromMetadataService} which does not have a template and just uses
 * the actual metadata value as the payload. It is also designed as the reverse form of {@link PayloadToMetadataService} and allows
 * you to take a piece of object metadata containing {@code byte[]} and make it the payload.
 * </p>
 * 
 * @config metadata-to-payload
 * 
 * @author lchan
 *
 */
@XStreamAlias("metadata-to-payload")
public class MetadataToPayloadService extends ServiceImp {


  /**
   * Enumeration of where the two types of metadata.
   * 
   */
  public enum MetadataSource
  {
    /**
     * Standard Metadata.
     * 
     */
    Standard {
      @Override
      InputStream getInputStream(AdaptrisMessage msg, String key) throws MessagingException {
        return new ReaderInputStream(new StringReader(msg.getMetadataValue(key)));
      }
    },
    /**
     * Object Metadata.
     * 
     */
    Object {
      @Override
      InputStream getInputStream(AdaptrisMessage msg, String key) throws MessagingException {
        return new ByteArrayInputStream((byte[]) msg.getObjectHeaders().get(key));
      }
    };
    
    abstract InputStream getInputStream(AdaptrisMessage msg, String key) throws MessagingException;
  };

  // Looking at the source of MimeUtility, 7bit/8bit don't do anything, and x-uuenc are just semaphores for uuencode.
  /**
   * The types of encoding supported.
   * 
   * @see MimeUtility#decode(InputStream, String)
   */
  public enum Encoding {
    Base64("base64"),
    Quoted_Printable("quoted-printable"),
    UUEncode("uuencode"),
    None(null) {
      InputStream unwrap(InputStream orig) {
        return orig;
      }
    };
    private String mimeEncoding;

    Encoding(String encoding) {
      mimeEncoding = encoding;
    }

    InputStream unwrap(InputStream orig) throws MessagingException {
      return MimeUtility.decode(orig, mimeEncoding);
    }
  }



  @NotBlank
  private String key;
  @NotNull
  private MetadataSource metadataSource;
  @NotNull
  @AutoPopulated
  private Encoding encoding;

  public MetadataToPayloadService() {
    setEncoding(Encoding.None);
  }

  public MetadataToPayloadService(String metadataKey, MetadataSource target) {
    this();
    setMetadataSource(target);
    setKey(metadataKey);
  }


  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    // MimeUtility should return the original InputStream stream if getContentEncoding is null.
    try (OutputStream out = msg.getOutputStream();
        InputStream in = getEncoding().unwrap((getMetadataSource().getInputStream(msg, getKey())))) {
      IOUtils.copy(in, out);
    } catch (Exception e) {
      ExceptionHelper.rethrowServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
  }



  @Override
  protected void initService() throws CoreException {

  }

  @Override
  protected void closeService() {

  }

  public MetadataSource getMetadataSource() {
    return metadataSource;
  }

  public void setMetadataSource(MetadataSource t) {
    this.metadataSource = Args.notNull(t, "Metadata Source");
  }

  public String getKey() {
    return key;
  }

  /**
   * Set the metadata key to which will form the payload.
   * 
   * @param key the key.
   */
  public void setKey(String key) {
    this.key = Args.notNull(key, "Metadata Key");
  }

  public Encoding getEncoding() {
    return encoding;
  }

  /**
   * Specify the encoding of the metadata.
   * 
   * @param enc the encoding, defaults to {@link Encoding#None}.
   */
  public void setEncoding(Encoding enc) {
    this.encoding = enc;
  }

}
