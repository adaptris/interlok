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

package com.adaptris.interlok.junit.scaffolding.jms;

import java.util.Arrays;
import java.util.List;
import com.adaptris.core.jms.AutoConvertMessageTranslator;
import com.adaptris.core.jms.BytesMessageTranslator;
import com.adaptris.core.jms.MapMessageTranslator;
import com.adaptris.core.jms.MessageTypeTranslator;
import com.adaptris.core.jms.ObjectMessageTranslator;
import com.adaptris.core.jms.TextMessageTranslator;
import com.adaptris.interlok.junit.scaffolding.BaseCase;

public abstract class JmsConfig {

  public static final long DEFAULT_TTL = System.currentTimeMillis() + 600000;
  public static final int HIGHEST_PRIORITY = 9;
  public static final int LOWEST_PRIORITY = 1;
  public static final String DEFAULT_PAYLOAD = "aaaaaaaa";

  private static final MessageTypeTranslator[] MESSAGE_TRANSLATORS =
  {
      new TextMessageTranslator(), new BytesMessageTranslator(), new ObjectMessageTranslator(), new MapMessageTranslator("key1"),
      new AutoConvertMessageTranslator()
  };

  public static final List<MessageTypeTranslator> MESSAGE_TRANSLATOR_LIST =
      Arrays
      .asList(MESSAGE_TRANSLATORS);

  public static boolean jmsTestsEnabled() {
    return Boolean.parseBoolean(BaseCase.PROPERTIES.getProperty("jms.tests.enabled", "true"));
  }
}
