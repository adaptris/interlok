/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.core.services.mime;

import static com.adaptris.util.text.mime.MimeConstants.HEADER_CONTENT_ID;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Multipart;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.text.mime.InputStreamDataSource;
import com.adaptris.util.text.mime.MimeConstants;
import com.adaptris.util.text.mime.MultiPartOutput;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Flatten any nested {@code MimeBodyParts} inside the payload.
 * 
 * <p>
 * Flattens the payload so that any nested {@code Multiparts} have their body parts added directly to the root multipart. This can
 * be useful if you are processing an email message; it can often contain both text and html versions of message body as a nested
 * multipart.
 * <p>
 * For example if you have a MIME message that contains 4 body parts; 3 that are plain text, and a 4th that is a multipart which
 * itself contains 3 text parts; then the resulting output will contain 6 parts; the 3 original plain text parts and the 3 nested
 * parts. Note that a {@code Content-Id} header will be generated for each part if it does not already exist. Headers will be
 * generally untouched, but boundary markers will change.
 * </p>
 * i.e. <pre> 
   {@code
Mime-Version: 1.0
Content-Type: multipart/mixed; boundary="----=_Part_3_815648243.1522235646062"

------=_Part_3_815648243.1522235646062
Content-Id: part1

...
------=_Part_3_815648243.1522235646062
Content-Id: part2

...
------=_Part_3_815648243.1522235646062
Content-Id: part3

...
------=_Part_3_815648243.1522235646062
Content-Type: multipart/mixed; boundary="----=_Part_2_1537805706.1522235646062"

------=_Part_2_1537805706.1522235646062
Content-Id: nested1

...
------=_Part_2_1537805706.1522235646062
Content-Id: nested2

...
------=_Part_2_1537805706.1522235646062
Content-Id: nested3

...
------=_Part_2_1537805706.1522235646062--

------=_Part_3_815648243.1522235646062--
   }
 * </pre> becomes <pre>
   {@code
Mime-Version: 1.0
Content-Type: multipart/mixed; boundary="----=_Part_4_18130400.1522235646074"

------=_Part_4_18130400.1522235646074
Content-Id: part1

...
------=_Part_4_18130400.1522235646074
Content-Id: part2

...
------=_Part_4_18130400.1522235646074
Content-Id: part3

...
------=_Part_4_18130400.1522235646074
Content-Id: nested1

...
------=_Part_4_18130400.1522235646074
Content-Id: nested2

...
------=_Part_4_18130400.1522235646074
Content-Id: nested3

...
------=_Part_4_18130400.1522235646074--
   }
 * </pre>
 * 
 * @config flatten-mime-parts
 */
@XStreamAlias("flatten-mime-parts")
@AdapterComponent
@ComponentProfile(summary = "Flatten a mime-message so all body parts are part of the root multipart", tag = "service,mime")
public class FlattenMimeParts extends ServiceImp {

  private static final GuidGenerator GUID = new GuidGenerator();

  private static final List<String> DISCARD = Arrays.asList(new String[]
  {
      MimeConstants.HEADER_CONTENT_LENGTH.toLowerCase(), MimeConstants.HEADER_CONTENT_TYPE.toLowerCase(),
  });

  public FlattenMimeParts() {
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try (InputStream in = msg.getInputStream()) {
      InputStreamDataSource src = new InputStreamDataSource(in);
      MimeMultipart mime = new MimeMultipart(src);
      MultiPartOutput output = new MultiPartOutput(msg.getUniqueId());
      List<BodyPart> parts = extract(mime);
      for (BodyPart p : parts) {
        output.addPart((MimeBodyPart) p, generateIfNoContentId(p));
      }
      addHeaders(src.getHeaders(), output);
      try (OutputStream out = msg.getOutputStream()) {
        output.writeTo(out);
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  private void addHeaders(InternetHeaders hdrs, MultiPartOutput out) {
    Enumeration e = hdrs.getAllHeaders();
    while (e.hasMoreElements()) {
      Header h = (Header) e.nextElement();
      if (!DISCARD.contains(h.getName().toLowerCase())) {
        out.setHeader(h.getName(), h.getValue());
      }
    }
  }

  private List<BodyPart> extract(Multipart m) throws Exception {
    List<BodyPart> parts = new ArrayList<>();
    for (int i = 0; i < m.getCount(); i++) {
      BodyPart p = m.getBodyPart(i);
      if (p.isMimeType("multipart/*")) {
        parts.addAll(extract((Multipart) p.getContent()));
      } else {
        parts.add(p);
      }
    }
    return parts;
  }

  private String generateIfNoContentId(BodyPart p) throws Exception {
    String result = null;
    try {
      String[] s = Args.notNull(p.getHeader(HEADER_CONTENT_ID), HEADER_CONTENT_ID);
      result = s[0];
    } catch (IllegalArgumentException e) {

    }
    return StringUtils.defaultIfEmpty(result, GUID.getUUID());
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }
}
