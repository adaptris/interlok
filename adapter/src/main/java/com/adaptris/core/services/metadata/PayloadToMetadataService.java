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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
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
 * Takes the entire payload and writes it out to metadata (either object or normal metadata).
 * 
 * <p>If storing to {@link MetadataTarget#Object} then the raw byte[] will be stored in object metadata against the specified key.
 * If storing to {@link MetadataTarget#Standard} then the payload will be treated as a String (using the default character set
 * encoding); if storing as standard metadata, then you are encouraged to apply an encoding such as base64 using {@link
 * #setEncoding(Encoding)} to make sure that the payload can be treated as a String. The reason for only using the default charset
 * is to avoid complications when the data is actually XML and the encoding specification does not match the message's character
 * set.
 * </p>
 * 
 * @config payload-to-metadata
 * @license BASIC
 * @author lchan
 *
 */
@XStreamAlias("payload-to-metadata")
public class PayloadToMetadataService extends ServiceImp {

  /**
   * Enumeration of where the two types of metadata.
   * 
   */
  public enum MetadataTarget
 {
    /**
     * Standard Metadata.
     * 
     */
    Standard {
      @Override
      void apply(AdaptrisMessage msg, String key, ByteArrayOutputStream value) {
        msg.addMetadata(key, value.toString());
      }
    },
    /**
     * Object Metadata.
     * 
     */
    Object {
      @Override
      void apply(AdaptrisMessage msg, String key, ByteArrayOutputStream value) {
        msg.addObjectHeader(key, value.toByteArray());
      }
    };
    
    abstract void apply(AdaptrisMessage msg, String key, ByteArrayOutputStream value);
  };

  // Looking at the source of MimeUtility, 7bit/8bit don't do anything, and x-uuenc are just semaphores for uuencode.
  /**
   * The types of encoding supported.
   * 
   * @see MimeUtility#encode(OutputStream, String)
   * 
   */
  public enum Encoding {
    Base64("base64"),
    Quoted_Printable("quoted-printable"),
    UUEncode("uuencode"),
    None(null) {
      OutputStream wrap(OutputStream orig) {
        return orig;
      }
    };
    private String mimeEncoding;
    Encoding(String encoding) {
      mimeEncoding = encoding;
    }

    OutputStream wrap(OutputStream orig) throws MessagingException {
      return MimeUtility.encode(orig, mimeEncoding);
    }
  }


  @NotBlank
  private String key;
  @NotNull
  private MetadataTarget metadataTarget;
  @NotNull
  @AutoPopulated
  private Encoding encoding;

  public PayloadToMetadataService() {
    setEncoding(Encoding.None);
  }

  public PayloadToMetadataService(String metadataKey, MetadataTarget target) {
    this();
    setMetadataTarget(target);
    setKey(metadataKey);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    // MimeUtility should return the original output stream if getContentEncoding is null.
    try (InputStream in = msg.getInputStream(); OutputStream out = getEncoding().wrap(bytesOut)) {
      IOUtils.copy(in, out);
    } catch (Exception e) {
      ExceptionHelper.rethrowServiceException(e);
    }
    getMetadataTarget().apply(msg, getKey(), bytesOut);
  }

  @Override
  public void prepare() throws CoreException {
  }


  @Override
  public void init() throws CoreException {
  }

  @Override
  public void close() {
  }

  public MetadataTarget getMetadataTarget() {
    return metadataTarget;
  }

  public void setMetadataTarget(MetadataTarget t) {
    this.metadataTarget = Args.notNull(t, "Metadata Target");
  }

  public String getKey() {
    return key;
  }

  /**
   * Set the metadata key to store the current payload against.
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
   * Specify any encoding that should be applied to the payload before setting as metadata.
   * 
   * @param enc the encoding, defaults to {@link Encoding#None}.
   */
  public void setEncoding(Encoding enc) {
    this.encoding = Args.notNull(enc, "Encoding");
  }
}
