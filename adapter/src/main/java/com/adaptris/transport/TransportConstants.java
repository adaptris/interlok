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

package com.adaptris.transport;

/**
 * Constants.
 * <p>
 * These are generally expected to be configuration constants that are global to
 * all transport types. For instance, both SSL and some custom encrypted
 * connection wil require access to a keystore, hence the presence of constant
 * in this class.
 * </p>
 */
public interface TransportConstants {

  /**
   * The key within the configuration containing the keystore.
   * <p>
   * <code>transport.security.keystore.filename</code> and is expected to be
   * in a URL format with parameters that govern the type and password
   * </p>
   * 
   * @see com.adaptris.security.keystore.KeystoreFactory
   */
  String CONFIG_KEYSTORE_FILE = "transport.security.keystore.url";

  /**
   * The key within the configuration containing the keystore password.
   * <p>
   * <code>ttransport.security.keystore.password</code>
   */
  String CONFIG_KEYSTORE_PW = "transport.security.keystore.password";

  /**
   * The key within the configuration containing the private key password.
   * <p>
   * <code>transport.security.keystore.privatekeypassword</code>
   */
  String CONFIG_PRIVATE_KEY_PW = "transport.security.keystore.private.key.password";

}
