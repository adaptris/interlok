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
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link PartSelector} implementation that just selects an arbitrary part.
 * 
 * <p>
 * No guarantees are made about the behaviour of this implementation; it is simply included for completenes
 * </p>
 * 
 * @config mime-null-selector
 * 
 * @author lchan
 * 
 */
@XStreamAlias("mime-null-selector")
public class NullPartSelector implements PartSelector {

	public NullPartSelector() {
	}


  /**
   * Returns all the parts in the {@link MimeMultipart}.
   */
	@Override
	public List<MimeBodyPart> select(MimeMultipart in) throws MessagingException {
		Vector<MimeBodyPart> list = new Vector<MimeBodyPart>();
		for (int i=0;i < in.getCount(); i++){
			list.add((MimeBodyPart)in.getBodyPart(i));
		}
		return list;
	}

  /**
   * Returns the first part of the {@link BodyPartIterator}
   * 
   */
  @Override
  public MimeBodyPart select(BodyPartIterator in) throws MessagingException {
    return in.getBodyPart(0);
  }
}
