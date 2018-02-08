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

import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import org.apache.commons.io.IOUtils;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.security.util.SecurityUtil;
import com.adaptris.util.stream.DevNullOutputStream;
import com.adaptris.util.text.Base64ByteTranslator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Create a base64 hash of the payload based on the configurable algorithm and stores it as metadata.
 * 
 * @config payload-hashing-service
 * 
 * 
 * @author lchan
 */
@XStreamAlias("payload-hashing-service")
@AdapterComponent
@ComponentProfile(summary = "Hash the payload and store the base64 form of the hash against a metadata key",
    tag = "service,metadata")
@DisplayOrder(order = {"hashAlgorithm", "metadataKey"})
public class PayloadHashingService extends ServiceImp {

  @NotBlank
  private String hashAlgorithm;
  @NotBlank
  @AffectsMetadata
  private String metadataKey;

  public PayloadHashingService() {
    super();
    SecurityUtil.addProvider();
  }

  public PayloadHashingService(String hash, String metadataKey) {
    this();
    setHashAlgorithm(hash);
    setMetadataKey(metadataKey);
  }

  public void doService(AdaptrisMessage msg) throws ServiceException {
    InputStream in = null;
    OutputStream out = null;
    try {
      in = msg.getInputStream();
      MessageDigest digest = MessageDigest.getInstance(getHashAlgorithm());
      out = new DigestOutputStream(new DevNullOutputStream(), digest);
      IOUtils.copy(in, out);
      out.flush();
      byte[] hash = digest.digest();
      msg.addMetadata(getMetadataKey(), new Base64ByteTranslator().translate(hash));
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
    finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }
  }

  @Override
  protected void initService() throws CoreException {
    try {
      Args.notBlank(getHashAlgorithm(), "hashAlgorithm");
      Args.notBlank(getMetadataKey(), "metadataKey");
      MessageDigest.getInstance(getHashAlgorithm());
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void closeService() {

  }


  public String getHashAlgorithm() {
    return hashAlgorithm;
  }

  /**
   * Set the hashing algorithm to use.
   *
   * @param hashAlgorithm the algorithm, for example SHA256
   */
  public void setHashAlgorithm(String hashAlgorithm) {
    this.hashAlgorithm = Args.notBlank(hashAlgorithm, "hashAlgorithm");
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * Set the metadata key against which the one way hash is stored.
   *
   * @param metadataKey the metadata key
   */
  public void setMetadataKey(String metadataKey) {
    this.metadataKey = Args.notBlank(metadataKey, "metadataKey");
  }

  @Override
  public void prepare() throws CoreException {
  }


}
