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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.adaptris.core.util.Args;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * {@link PartSelector} implementation that parses a specific header examining the value to see if it matches the configured regular
 * expression.
 * 
 * @config mime-select-by-header
 * 
 * @author lchan
 * @author $Author: lchan $
 */
@JacksonXmlRootElement(localName = "mime-select-by-header")
@XStreamAlias("mime-select-by-header")
public class SelectByHeader implements PartSelector {

  @NotNull
  @NotBlank
  private String headerName;
  @NotNull
  @NotBlank
  private String headerValueRegExp;

  public SelectByHeader() {
  }

  public SelectByHeader(String header, String regexp) {
    this();
    setHeaderName(header);
    setHeaderValueRegExp(regexp);
  }

  @Override
  public MimeBodyPart select(BodyPartIterator m) throws MessagingException {
    MimeBodyPart result = null;
    assertConfig();
    outer: while (m.hasNext()) {
      MimeBodyPart p = m.next();
      String[] values = p.getHeader(getHeaderName());
      if (values != null) {
        for (String value : values) {
          if (value.matches(getHeaderValueRegExp())) {
            result = p;
            break outer;
          }
        }
      }
    }
    return result;
  }

  private void assertConfig() throws MessagingException {
    try {
      Args.notBlank(getHeaderName(), "headerName");
      Args.notBlank(getHeaderValueRegExp(), "headerRegex");
    } catch (IllegalArgumentException e) {
      throw new MessagingException("No configured header / regexp");
    }
  }

  @Override
  /**
   * Functionality not supported in this PartSelector
   */
  public List<MimeBodyPart> select(MimeMultipart in) throws MessagingException {
  	throw new MessagingException("Functionality not currently supported in this PartSelector");
  }

  /**
   * @return the headerName
   */
  public String getHeaderName() {
    return headerName;
  }

  /**
   * Specify the header name whose value will be examined .
   *
   * @param s the headerName to set, e.g. "Content-Type"
   */
  public void setHeaderName(String s) {
    headerName = s;
  }

  /**
   * @return the headerValueRegExp
   */
  public String getHeaderValueRegExp() {
    return headerValueRegExp;
  }

  /**
   * Set the value of the regular expression that will be used to match against
   * the header value.
   *
   * @param s the headerValueRegExp to set
   */
  public void setHeaderValueRegExp(String s) {
    headerValueRegExp = s;
  }
}
