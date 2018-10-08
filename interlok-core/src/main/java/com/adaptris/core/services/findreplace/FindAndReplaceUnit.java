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

package com.adaptris.core.services.findreplace;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A unit of configuration for doing find and replace.
 * 
 * @config find-and-replace-unit
 * @author lchan
 * 
 */
@XStreamAlias("find-and-replace-unit")
public class FindAndReplaceUnit {

  private ReplacementSource find;
  private ReplacementSource replace;
  
  public FindAndReplaceUnit() {
    
  }

  public FindAndReplaceUnit(ReplacementSource source, ReplacementSource dest) {
    this();
    setFind(source);
    setReplace(dest);
  }

  public ReplacementSource getFind() {
    return find;
  }

  public void setFind(ReplacementSource find) {
    this.find = find;
  }

  public ReplacementSource getReplace() {
    return replace;
  }

  public void setReplace(ReplacementSource replace) {
    this.replace = replace;
  }
}
