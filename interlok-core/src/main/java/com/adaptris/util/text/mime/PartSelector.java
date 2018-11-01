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

package com.adaptris.util.text.mime;

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import com.adaptris.annotation.Removal;


/**
 * Select a specific MimeBodyPart from a Mime Multipart.
 * 
 */
public interface PartSelector {

  /**
   * Select the body part that should form the AdaptrisMessage payload.
   * 
   * @param in a MultiPartInput whose iterator returns a MimeBodyPart.
   * @return the MimeBodyPart that should be the body, or null if no match found.
   * @deprecated since 3.7.2.
   * @implSpec The default implementation will return null.
   */
  @Deprecated
  @Removal(version = "3.10.0")
  default MimeBodyPart select(MultiPartInput in) throws MessagingException {
    return null;
  }

  /**
   * Select the body part that should form the AdaptrisMessage payload.
   * 
   * @return the MimeBodyPart that should be the body, or null if no match found.
   */
  MimeBodyPart select(BodyPartIterator in) throws MessagingException;

  /**
   * Select the body part that should form the AdaptrisMessage payload.
   * 
   * @param in	a MimeMultipart
   * @return a list of MimeBodyPart that should be the body, empty if no match
   *         found.
   */
  List<MimeBodyPart> select(MimeMultipart in) throws MessagingException;

}
