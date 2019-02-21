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

package com.adaptris.core.http.jetty;

import com.adaptris.annotation.Removal;
import com.adaptris.core.util.LoggingHelper;

/**
 * 
 * @deprecated since 3.6.5 use {@link JettyMessageConsumer} instead.
 */
@Deprecated
@Removal(version = "3.9.0", message = "Use JettyMessageConsumer instead")
public class MessageConsumer extends JettyMessageConsumer {

  private static boolean warningLogged = false;

  public MessageConsumer() {
    super();
    LoggingHelper.logDeprecation(warningLogged, ()-> { warningLogged=true;}, this.getClass().getSimpleName(), JettyMessageConsumer.class.getName());
  }
}
