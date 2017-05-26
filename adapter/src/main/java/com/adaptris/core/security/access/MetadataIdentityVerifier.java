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
package com.adaptris.core.security.access;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Identity verification based on matching metadata keys against other metadata keys.
 * <p>
 * This {@link IdentityVerifier} iterates over each entry in the identity map from {@link IdentityBuilder#build(AdaptrisMessage)}
 * and compares the value with the corresponding value from message metadata.
 * </p>
 * <p>
 * The key in {@link #getMetadataMap()} needs to match the key in the identity map. The value refers to the metadata key that will
 * be used to perform the match. In the event that the {@link IdentityBuilder} builds a map containing non-strings; then results are
 * undefined (most likely {@link #validate(IdentityBuilder, AdaptrisMessage)} will return false).
 * </p>
 * 
 * @config simple-metadata-user-identity-verifier
 */
@XStreamAlias("simple-metadata-user-identity-verifier")
public class MetadataIdentityVerifier extends IdentityVerifierImpl {

  @NotNull
  @Valid
  @AutoPopulated
  private KeyValuePairSet metadataMap;

  public MetadataIdentityVerifier() {
    setMetadataMap(new KeyValuePairSet());
  }

  public MetadataIdentityVerifier(KeyValuePairSet set) {
    this();
    setMetadataMap(set);
  }

  @Override
  public boolean validate(IdentityBuilder builder, AdaptrisMessage msg) {
    boolean rc = false;
    try {
      int count = 0;
      Map<String, Object> identity = builder.build(msg);
      for (Map.Entry<String, Object> entry : identity.entrySet()) {
        count += validate(entry, msg) ? 1 : 0;
      }
      // So, everything in the identity map was validated.
      rc = (count == identity.size());
    }
    catch (ServiceException e) {
      rc = false;
    }
    return rc;
  }

  private boolean validate(Map.Entry<String, Object> entry, AdaptrisMessage msg) throws ValidationException {
    String metadataKey = ensureNotBlank(getMetadataMap().getValue(entry.getKey()),
        String.format("'%s' not in verification map", entry.getKey()));
    String metadataValue = ensureNotBlank(msg.getMetadataValue(metadataKey),
        String.format("'%s' not in msg", metadataKey));
    return metadataValue.equals(entry.getValue());
  }

  public KeyValuePairSet getMetadataMap() {
    return metadataMap;
  }


  private String ensureNotBlank(String value, String errMsg) throws ValidationException {
    if (isBlank(value)) {
      throw new ValidationException(errMsg);
    }
    return value;
  }

  /**
   * Set the map to verify against.
   * <p>
   * The key in this instance should match the key returned by {@link IdentityBuilder#build(AdaptrisMessage)} and the value the
   * corresponding metadata key that you want to match against.
   * </p>
   * 
   * @param m the map;
   */
  public void setMetadataMap(KeyValuePairSet m) {
    this.metadataMap = Args.notNull(m, "metadataMap");
  }

  private class ValidationException extends ServiceException {
    private static final long serialVersionUID = 2017052501L;


    public ValidationException(String description) {
      super(description);
    }

  }
}
