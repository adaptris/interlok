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

import javax.mail.URLName;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MailReceiverFactory} that uses standard Javamail functionality and supports both POP3 + IMAP
 * 
 * @config javamail-receiver-factory
 * @author lchan
 * @see MailboxClient
 */
@XStreamAlias("javamail-receiver-factory")
public class JavamailReceiverFactory implements MailReceiverFactory {
  @NotNull
  @AutoPopulated
  @Valid
  private KeyValuePairSet sessionProperties;

  public JavamailReceiverFactory() {
    setSessionProperties(new KeyValuePairSet());
  }

  @Override
  public MailReceiver createClient(URLName url) {
    return configure(new MailboxClient(url));
  }

  public KeyValuePairSet getSessionProperties() {
    return sessionProperties;
  }

  /**
   * Set the session properties to apply to any {@link MailReceiver} instances.
   * 
   * @param kp the properties
   */
  public void setSessionProperties(KeyValuePairSet kp) {
    sessionProperties = Args.notNull(kp, "sessionProperties");
  }

  private MailboxClient configure(MailboxClient mbox) {
    mbox.setSessionProperties(KeyValuePairSet.asProperties(getSessionProperties()));
    return mbox;
  }
}
