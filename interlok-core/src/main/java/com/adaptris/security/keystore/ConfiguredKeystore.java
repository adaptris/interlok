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

package com.adaptris.security.keystore;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.validation.constraints.ConfigDeprecated;
import java.io.IOException;

import java.util.Optional;
import javax.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.security.exc.AdaptrisSecurityException;

/**
 * Abstract class for presenting arbitary configuation as KeystoreLocation
 * objects.
 *
 * @author lchan
 * @author $Author: lchan $
 */
@NoArgsConstructor
public abstract class ConfiguredKeystore {

  protected transient Logger logR = LoggerFactory.getLogger(this.getClass().getName());
  /** The keystore factory to use when accessing this keystore.
   *  <p>There is no reason for you to configure this and this will be removed w/o warning.</p>
   */
  @ConfigDeprecated(removalVersion = "5.0", message = "There is no reason to configure the keystore factory", groups = Deprecated.class)
  @AdvancedConfig(rare=true)
  @Valid
  @Getter
  @Setter
  private KeystoreFactory keystoreFactory = null;
  private transient KeystoreProxy proxy = null;

  public abstract KeystoreLocation asKeystoreLocation()
      throws AdaptrisSecurityException;

  protected static KeystoreFactory getKeystoreFactory(ConfiguredKeystore configured) {
    return Optional.ofNullable(configured).map((k)-> k.getKeystoreFactory()).orElse(KeystoreFactory.getDefault());
  }

  public final KeystoreProxy asKeystoreProxy()
      throws AdaptrisSecurityException, IOException {
    if (proxy == null) {
      KeystoreLocation ks = asKeystoreLocation();
      KeystoreFactory ksf = getKeystoreFactory(this);
      proxy = ksf.create(ks);
      proxy.load();
    }
    return proxy;
  }

}
