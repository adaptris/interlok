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

package com.adaptris.core;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.Date;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Implementation of FileNameCreator that uses {@link String#format(String, Object...)}.
 * 
 * <p>
 * This is intended as a replacement for StandardFilenameCreator which is now deprecated. It makes use of the JDK1.5
 * {@link String#format(String, Object...)} method which will allow you to generate an arbitary filename rather than being
 * restricted to a prefix + suffix + timestamp.
 * </p>
 * <p>
 * The following rules will apply when you are constructing your formatted filename
 * <ul>
 * <li>The format is passed directly to {@link java.util.Formatter}</li>
 * <li>The message id is always passed in as the first parameter</li>
 * <li>The timestamp (as a {@link java.util.Date}) is passed in as the second parameter</li>
 * <li>If not explicitly configured, then the format string defaults to "%1$s" which simply equates to the message-id</li>
 * </ul>
 * </p>
 * <h2>Examples</h2>
 * <p>
 * If the message in question has a unique-id of b740ddae-ff4d-4576-9bc9-51d0f14b6df4 and the current date and time is
 * "2012-05-02 09:41:23" then we would use to following formats to generate output that matches our criteria.
 * <ul>
 * <li>"message.xml" would give us <strong>message.xml</strong> (not recommended due to the possibly of naming conflicts)</li>
 * <li>"%1$s.xml" would give us <strong>b740ddae-ff4d-4576-9bc9-51d0f14b6df4.xml</strong></li>
 * <li>"message-%1$s-stardate-%2$tF.xml" would give us
 * <strong>message-b740ddae-ff4d-4576-9bc9-51d0f14b6df4-stardate-2012-05-02.xml</strong></li>
 * <li>"%2$tF_%1$s.xml" would give us <strong>2012-05-02_b740ddae-ff4d-4576-9bc9-51d0f14b6df4.xml</strong></li>
 * <li>"message-%1$s-%2$tA.xml" would give us <strong>message-b740ddae-ff4d-4576-9bc9-51d0f14b6df4-Wednesday.xml</strong></li>
 * <li>"%2$tF-%2$tH%2$tM_%1$s.xml" would give us <strong>2012-05-02-0941_b740ddae-ff4d-4576-9bc9-51d0f14b6df4.xml</strong></li>
 * <li>"message-%1$s-%2$tQ.xml" would give us <strong>message-b740ddae-ff4d-4576-9bc9-51d0f14b6df4-1335948092985.xml</strong></li>
 * </ul>
 * </p>
 * 
 * 
 @config formatted-filename-creator
 * 
 * @see String#format(String, Object...)
 * @see java.util.Formatter
 */
@XStreamAlias("formatted-filename-creator")
@DisplayOrder(order = {"filenameFormat"})
public class FormattedFilenameCreator implements FileNameCreator {

  @InputFieldHint(expression = true)
  private String filenameFormat;

  public FormattedFilenameCreator() {
    setFilenameFormat("%1$s");
  }

  @Override
  public String createName(AdaptrisMessage msg) {
    return String.format(msg.resolve(getFilenameFormat()), msg.getUniqueId(), new Date());
  }

  public String getFilenameFormat() {
    return filenameFormat;
  }

  public void setFilenameFormat(String s) {
    if (isEmpty(s)) {
      throw new IllegalArgumentException("FilenameFormat is null/empty");
    }
    filenameFormat = s;
  }

}
