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

package com.adaptris.util.text;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Implementation of NullConverter that simply returns the value passed in.
 * 
 * @config null-pass-through-converter
 * 
 * @author lchan
 * 
 */
@XStreamAlias("null-pass-through-converter")
public class NullPassThroughConverter implements NullConverter {

  @Override
  public <T> T convert(T t) {
    return t;
  }

}
