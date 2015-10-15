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

package com.adaptris.core.services.system;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Returns a fixed value for this command line argument.
 * 
 * @config system-command-static-argument
 * 
 * @author sellidge
 */
@XStreamAlias("system-command-static-argument")
public class StaticCommandArgument implements CommandArgument {

  private String value;

  public StaticCommandArgument() {

  }
  
  public StaticCommandArgument(String arg) {
    this();
    setValue(arg);
  }

  @Override
  public String retrieveValue(AdaptrisMessage msg) {
    return getValue();
  }

  public String getValue() {
    return value;
  }

  /**
   * The value to be returned
   * @param value
   */
  public void setValue(String value) {
    this.value = value;
  }
}
