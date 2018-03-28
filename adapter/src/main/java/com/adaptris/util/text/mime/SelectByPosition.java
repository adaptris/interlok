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

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.validation.constraints.Min;

import com.adaptris.annotation.AutoPopulated;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Selects a MimeBodyPart based on its position within the Multipart.
 * 
 * @config mime-select-by-position
 * 
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("mime-select-by-position")
public class SelectByPosition implements PartSelector {

  @AutoPopulated
  @Min(0)
  private int position;

  public SelectByPosition() {
    setPosition(0);
  }

  public SelectByPosition(int i) {
    this();
    setPosition(i);
  }


  @Override
  @SuppressWarnings("deprecation")
  public MimeBodyPart select(MultiPartInput m) throws MessagingException {
    return m.getBodyPart(getPosition());
  }

  @Override
  public MimeBodyPart select(BodyPartIterator m) throws MessagingException {
    return m.getBodyPart(getPosition());
  }

  @Override
  public List<MimeBodyPart> select(MimeMultipart in) throws MessagingException {
    if (getPosition() >= in.getCount()) {
      return new ArrayList<MimeBodyPart>();
    }
    ArrayList<MimeBodyPart> list = new ArrayList<MimeBodyPart>();
    list.add((MimeBodyPart)in.getBodyPart(getPosition()));
    return list;
  }


  /**
   * @return the position
   */
  public int getPosition() {
    return position;
  }

  /**
   * The position of the MimeBodyPart to select within the multi part.
   *
   * @param i the position to select, starting from 0
   */
  public void setPosition(int i) {
    position = i;
  }

}
