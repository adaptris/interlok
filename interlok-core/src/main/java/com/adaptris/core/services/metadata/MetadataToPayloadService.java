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
import java.nio.charset.Charset;
import javax.mail.MessagingException;
import javax.validation.constraints.NotBlank;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.UnresolvedMetadataException;
import com.adaptris.core.metadata.MetadataResolver;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.EncodingHelper.Encoding;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Takes a metadata value and sets that as the payload.
 * 
 * <p>
 * This can be treated as a simplified form of {@link PayloadFromMetadataService} which does not
 * have a template and just uses the actual metadata value as the payload. It is also designed as
 * the reverse form of {@link PayloadToMetadataService} and allows you to take a piece of object
 * metadata containing {@code byte[]} and make it the payload.
 * </p>
 * <p>
 * This service will throw an error if the target metadata item does not exist.
 * </p>
 * <p>
 * This service also supports a resolvable metadata key via
 * {@link com.adaptris.core.metadata.MetadataResolver}; and if the metadata source is
 * {@link MetadataSource#Standard} then an additional
 * {@link AdaptrisMessage#resolve(String, boolean)} step is executed.
 * </p>
 * 
 * @config metadata-to-payload
 * 
 * @author lchan
 *
 */
@XStreamAlias("metadata-to-payload")
@AdapterComponent
@ComponentProfile(summary = "Takes a metadata value and sets that as the payload", tag = "service,metadata")
@DisplayOrder(order = {"key", "encoding", "metadataSource"})
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
        String resolvedKey = MetadataResolver.resolveKey(msg, key);
        String value = msg.resolve(msg.getMetadataValue(resolvedKey), true);
        if (value == null) {
          throw new UnresolvedMetadataException(
              "Metadata key (" + resolvedKey + ") does not exist.");
        }
        return new ReaderInputStream(new StringReader(value), Charset.defaultCharset());
      }
    },
    /**
     * Object Metadata.
     * 
     */
    Object {
      @Override
      InputStream getInputStream(AdaptrisMessage msg, String key) throws MessagingException {
        String resolvedKey = MetadataResolver.resolveKey(msg, key);
        if(msg.getObjectHeaders().containsKey(resolvedKey)) 
          return new ByteArrayInputStream((byte[]) msg.getObjectHeaders().get(resolvedKey));
        else
          throw new UnresolvedMetadataException("Object metadata key (" + resolvedKey + ") does not exist.");
      }
    };
    
    abstract InputStream getInputStream(AdaptrisMessage msg, String key) throws MessagingException;
  };


  @NotBlank
  @InputFieldHint(expression = true)
  private String key;
  @InputFieldDefault(value = "Standard")
  private MetadataSource metadataSource;
  @InputFieldDefault(value = "None")
  private Encoding encoding;

  public MetadataToPayloadService() {
  }

  public MetadataToPayloadService(String metadataKey, MetadataSource target) {
    this();
    setMetadataSource(target);
    setKey(metadataKey);
  }


  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try (InputStream in = encoding().wrap(source().getInputStream(msg, getKey())); OutputStream out = msg.getOutputStream();) {
      IOUtils.copy(in, out);
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
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

  private MetadataSource source() {
    return ObjectUtils.defaultIfNull(getMetadataSource(), MetadataSource.Standard);
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

  private Encoding encoding() {
    return ObjectUtils.defaultIfNull(getEncoding(), Encoding.None);
  }
}
