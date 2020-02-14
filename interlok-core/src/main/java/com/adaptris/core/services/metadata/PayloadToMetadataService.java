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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.EncodingHelper.Encoding;
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
 * 
 * @author lchan
 *
 */
@XStreamAlias("payload-to-metadata")
@AdapterComponent
@ComponentProfile(summary = "Take the entire payload and store it against a metadata key", tag = "service,metadata")
@DisplayOrder(order = {"key", "encoding", "metadataTarget"})
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


  @NotBlank
  @AffectsMetadata
  private String key;
  @NotNull
  @AutoPopulated
  @InputFieldDefault(value = "Standard")
  private MetadataTarget metadataTarget;
  @AdvancedConfig
  @InputFieldDefault(value = "None")
  private Encoding encoding;

  public PayloadToMetadataService() {
    setMetadataTarget(MetadataTarget.Standard);
  }

  public PayloadToMetadataService(String metadataKey, MetadataTarget target) {
    this();
    setMetadataTarget(target);
    setKey(metadataKey);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    try (InputStream in = msg.getInputStream(); OutputStream out = encoding().wrap(bytesOut)) {
      IOUtils.copy(in, out);
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    getMetadataTarget().apply(msg, getKey(), bytesOut);
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

  private Encoding encoding() {
    return ObjectUtils.defaultIfNull(getEncoding(), Encoding.None);
  }

}
