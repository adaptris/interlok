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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.CoreException;
import com.adaptris.util.text.Base64ByteTranslator;
import com.adaptris.util.text.ByteTranslator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Hashes the metadata value stored against a metadata key.
 * <p>
 * Each matching metadata key from {@link ReformatMetadata#getMetadataKeyRegexp()} will be hashed, and the value overwitten with the
 * hash after translating it into a String with the specified {@link ByteTranslator}
 * </p>
 * 
 * @config metadata-hashing-service
 * 
 * 
 * 
 */
@XStreamAlias("metadata-hashing-service")
@AdapterComponent
@ComponentProfile(summary = "Hash a metadata value, and store it", tag = "service,metadata")
@DisplayOrder(order = {"metadataKeyRegexp", "hashAlgorithm", "byteTranslator"})
public class MetadataHashingService extends ReformatMetadata {
  private static final String DEFAULT_HASH_ALG = "SHA1";
  @NotBlank
  private String hashAlgorithm;
  @NotNull
  @Valid
  private ByteTranslator byteTranslator;

  public MetadataHashingService() {
    super();
    setHashAlgorithm(DEFAULT_HASH_ALG);
    setByteTranslator(new Base64ByteTranslator());
  }

  public MetadataHashingService(String regexp) {
    super(regexp);
    setHashAlgorithm(DEFAULT_HASH_ALG);
    setByteTranslator(new Base64ByteTranslator());
  }

  public MetadataHashingService(String regexp, String hash, ByteTranslator translator) {
    this();
    setMetadataKeyRegexp(regexp);
    setHashAlgorithm(hash);
    setByteTranslator(translator);
  }

  @Override
  protected void initService() throws CoreException {
    try {
      MessageDigest d = MessageDigest.getInstance(getHashAlgorithm());
    } catch (NoSuchAlgorithmException e) {
      throw new CoreException(e.getMessage(), e);
    }
    super.initService();
  }

  @Override
  protected void closeService() {
    super.closeService();
  }


  @Override
  protected String reformat(String s, String charEncoding) throws Exception {
    return getByteTranslator().translate(MessageDigest.getInstance(getHashAlgorithm()).digest(toBytes(s, charEncoding)));
  }

  public final String getHashAlgorithm() {
    return hashAlgorithm;
  }

  public final void setHashAlgorithm(String hashAlg) {
    if (isEmpty(hashAlg)) throw new IllegalArgumentException("HashAlg may not be null");
    this.hashAlgorithm = hashAlg;
  }

  private byte[] toBytes(String metadataValue, String charset) throws UnsupportedEncodingException {
    if (!isEmpty(charset)) {
      return metadataValue.getBytes(charset);
    }
    return metadataValue.getBytes();
  }

  public final ByteTranslator getByteTranslator() {
    return byteTranslator;
  }

  /**
   * Specify how to translate the resulting byte array from the hash into a String.
   * 
   * @param translator the translator;
   */
  public final void setByteTranslator(ByteTranslator translator) {
    if (translator == null) throw new IllegalArgumentException("Translator may not be null");
    this.byteTranslator = translator;
  }

}
