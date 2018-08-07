/*
 * Copyright Adaptris Ltd.
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

package com.adaptris.core.metadata;

import com.adaptris.core.MetadataElement;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Metadata Filter implementation that decodes all values that match the specified key.
 * <p>
 * Note that keys that do not match will still be returned; just that keys that do match {@link #getRegexp()} will have
 * {@link Password#decode(String)} applied to the values. Please note that a new {@code MetadataElement} is created as a result of
 * the encode/deocde process, so any changes may not be reflected in the original message.
 * </p>
 * 
 * @config password-decoder-metadata-filter
 * @since 3.8.1
 */
@XStreamAlias("password-decoder-metadata-filter")
public class PasswordDecodeMetadataFilter extends PasswordMetadataFilter {

  protected MetadataElement handlePassword(MetadataElement element) throws PasswordException {
    return new MetadataElement(element.getKey(), Password.decode(ExternalResolver.resolve(element.getValue())));
  }
}
