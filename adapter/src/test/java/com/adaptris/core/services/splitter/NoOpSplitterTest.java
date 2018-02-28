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
package com.adaptris.core.services.splitter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class NoOpSplitterTest {

  @Test
  public void testSplitMessage() throws Exception {
    NoOpSplitter splitter = new NoOpSplitter();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    List<AdaptrisMessage> split = splitter.splitMessage(msg);
    assertEquals(1, split.size());
    assertEquals(msg, split.get(0));
  }

}
