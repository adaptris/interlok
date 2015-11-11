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

package com.adaptris.core.management.properties;

import com.adaptris.security.password.Password;

/**
 * Decodes a password using {@link com.adaptris.security.password.Password#decode(String)}
 * <p>
 * Decodes system properties that are stored with the {password} scheme.
 * </p>
 * <code>
 * <pre>
 sysprop.encrypted={password}PW:AAAAEDNPp8M3xBUiU+goN1cmjBYAAAAQorWHploKWvTb5bmjjgiCWQAAABCa6cnOef76qd67FXsgN4nV
 * </pre>
 * </code>
 * 
 * @author lchan
 * 
 */
public class PasswordDecoder implements Decoder {

  @Override
  public String decode(String value) throws Exception {
    return Password.decode(value);
  }

}
