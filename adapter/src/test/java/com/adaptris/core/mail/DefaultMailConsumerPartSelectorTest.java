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

package com.adaptris.core.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.Poller;
import com.adaptris.core.QuartzCronPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.mail.JavamailReceiverFactory;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.text.mime.PartSelector;
import com.adaptris.util.text.mime.SelectByContentId;
import com.adaptris.util.text.mime.SelectByHeader;
import com.adaptris.util.text.mime.SelectByPosition;

public class DefaultMailConsumerPartSelectorTest extends MailConsumerExample {

  public DefaultMailConsumerPartSelectorTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected String createBaseFileName(Object object) {
    DefaultMailConsumer consumer = (DefaultMailConsumer) ((StandaloneConsumer) object).getConsumer();

    return super.createBaseFileName(object) + consumer.getPartSelector().getClass().getSimpleName();
  }

  private StandaloneConsumer createConfigExample(Poller pollerImp, PartSelector partSelector) {
    DefaultMailConsumer pop3 = new DefaultMailConsumer();
    pop3.setPartSelector(partSelector);
    pop3.setDestination(new ConfiguredConsumeDestination("pop3://username:password@server:110/INBOX", "FROM=optionalFilter,"
        + "SUBJECT=optionalFilter," + "RECIPIENT=optionalFilter"));
    JavamailReceiverFactory fac = new JavamailReceiverFactory();
    fac.getSessionProperties().addKeyValuePair(new KeyValuePair("mail.smtp.starttls.enable", "true"));
    fac.getSessionProperties().addKeyValuePair(new KeyValuePair("mail.pop3.starttls.enable", "true"));
    pop3.setMailReceiverFactory(fac);
    pop3.setPoller(pollerImp);
    return new StandaloneConsumer(pop3);
  }
  /**
   * @see com.adaptris.core.ExampleConfigCase#retrieveObjectForSampleConfig()
   */
  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  /**
   * @see com.adaptris.core.ExampleConfigCase#retrieveObjectForSampleConfig()
   */
  @Override
  protected List retrieveObjectsForSampleConfig() {
    List<StandaloneConsumer> result = new ArrayList<StandaloneConsumer>();
    result.add(createConfigExample(new FixedIntervalPoller(new TimeInterval(10L, TimeUnit.MINUTES)), new SelectByPosition(1)));
    result.add(createConfigExample(new QuartzCronPoller("00 */10 * * * ?"), new SelectByPosition(1)));

    result.add(createConfigExample(new FixedIntervalPoller(new TimeInterval(10L, TimeUnit.MINUTES)), new SelectByContentId(
        "Content-Id-Will-Equal-This")));
    result.add(createConfigExample(new QuartzCronPoller("00 */10 * * * ?"), new SelectByContentId("Content-Id-Will-Equal-This")));

    result.add(createConfigExample(new FixedIntervalPoller(new TimeInterval(10L, TimeUnit.MINUTES)), new SelectByHeader("Header",
        "RegExpMatch")));
    result.add(createConfigExample(new QuartzCronPoller("00 */10 * * * ?"), new SelectByHeader("Header", "RegExpMatch")));

    return result;
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj)
        + "<!-- This shows an example of using a PartSelector which allows you to selectively"
        + "\nignore some parts of the email depending on configuration; check the javadocs for the"
        + "\nvarious PartSelector implementations for more imformation. \n"
        + "\nTypical configurations may select a part by index (indexing starts from 0), by content id or by matching against a header."
        + "\n\n-->\n";
  }

}
