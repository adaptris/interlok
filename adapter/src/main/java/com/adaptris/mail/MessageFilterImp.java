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

package com.adaptris.mail;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;

abstract class MessageFilterImp implements MessageFilter {
  
  protected transient MatchProxy matcher;

  private MessageFilterImp() {

  }

  MessageFilterImp(MatchProxy h) {
    this();
    matcher = h;
  }

  List<String> toList(Address[] addr) {
    List<String> result = new ArrayList<>();
    if (addr != null) {
      for (Address r : addr) {
        result.add(r.toString());
      }
    }
    return result;
  }

  boolean matches(List<String> list) {
    for (String s : list) {
      if (matcher.matches(s)) {
        return true;
      }
    }
    return false;
  }

  abstract List<String> getHeaders(Message m) throws MessagingException;

  public final boolean accept(Message m) throws MessagingException {
    // no matcher, always true.
    if (matcher == null) {
      return true;
    }
    return matches(getHeaders(m));
  }

}
