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

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairCollection;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Copies metadata from one key to another.
 * <p>
 * Updates message metadata by
 * <ul>
 * <li>obtaining the metadata value stored against the 'original' key configured as the key in the metadata-keys section</li>
 * <li>copying this value to the 'new' key configured as the value of the associated metadata-keys element</li>.
 * </p>
 * <p>
 * If the configured 'original' metadata key does not exist, then metadata is not copied, if the 'new' metadata key already exists,
 * it is overwritten.
 * </p>
 * 
 * @config copy-metadata-service
 * 
 * 
 */
@XStreamAlias("copy-metadata-service")
@AdapterComponent
@ComponentProfile(summary = "Copy metadata values to other metadata keys", tag = "service,metadata")
public class CopyMetadataService extends ServiceImp {

  @NotNull
  @AutoPopulated
  private KeyValuePairCollection metadataKeys;

  public CopyMetadataService() {
    setMetadataKeys(new KeyValuePairCollection());
  }

  /**
   * {@inheritDoc}
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    for (KeyValuePair k : getMetadataKeys()) {
      String value = msg.getMetadataValue(k.getKey());
      if (value != null) {
        msg.addMetadata(k.getValue(), value);
        log.debug("found metadata [" + value + "] against key [" + k.getKey()
            + "] copied to key [" + k.getValue() + "]");
      }
    }
  }


  @Override
  protected void initService() throws CoreException {

  }

  @Override
  protected void closeService() {

  }


  /**
   * <p>
   * Returns a {@linkplain KeyValuePairCollection} in which the key is the key
   * to look up, and the value is the key to store whatever was found against.
   * </p>
   *
   * @return a {@linkplain KeyValuePairCollection}
   */
  public KeyValuePairCollection getMetadataKeys() {
    return metadataKeys;
  }

  /**
   * <p>
   * Sets a {@linkplain KeyValuePairCollection} in which the key is the key to
   * look up, and the value is the key to store whatever was found against the
   * key.
   * </p>
   *
   * @param m a {@linkplain KeyValuePairCollection}
   */
  public void setMetadataKeys(KeyValuePairCollection m) {
    if (m == null) {
      throw new IllegalArgumentException("null param");
    }
    this.metadataKeys = m;
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer(super.toString());
    result.append(this.getMetadataKeys());

    return result.toString();
  }

  @Override
  public void prepare() throws CoreException {
  }

}
