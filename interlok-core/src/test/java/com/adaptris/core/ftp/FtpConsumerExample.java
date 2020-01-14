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

package com.adaptris.core.ftp;

import com.adaptris.core.AdaptrisPollingConsumer;
import com.adaptris.core.ConsumerCase;
import com.adaptris.core.StandaloneConsumer;

public abstract class FtpConsumerExample extends ConsumerCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "FtpConsumerExamples.baseDir";

  public FtpConsumerExample() {
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected String createBaseFileName(Object object) {
    AdaptrisPollingConsumer c = (AdaptrisPollingConsumer) ((StandaloneConsumer) object).getConsumer();
    return super.createBaseFileName(object) + "-" + c.getPoller().getClass().getSimpleName();
  }
}
