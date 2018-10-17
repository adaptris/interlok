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

import java.io.IOException;
import java.io.OutputStream;

import com.adaptris.security.exc.AdaptrisSecurityException;


/** A Readonly keystore.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
abstract class ReadonlyKeystore extends KeystoreLocationImp {

  /**
   * @see com.adaptris.security.keystore.KeystoreLocation#isWriteable()
   */
  public boolean isWriteable() {
    return false;
  }

  /**
   * @see com.adaptris.security.keystore.KeystoreLocation#openOutput()
   */
  public OutputStream openOutput()
      throws IOException, AdaptrisSecurityException {
    throw new IOException("Cannot open output to ReadOnly keystore");
  }

}
