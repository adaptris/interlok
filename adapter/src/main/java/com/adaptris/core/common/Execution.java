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

package com.adaptris.core.common;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.core.util.Args;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.config.DataOutputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("standard-execution")
public class Execution {
  
  @NotNull
  @Valid
  private DataInputParameter<String> source;
  
  @NotNull
  @Valid
  private DataOutputParameter<String> target;
  
  public Execution() {
    
  }

  public Execution(DataInputParameter<String> src, DataOutputParameter<String> t) {
    this();
    setSource(src);
    setTarget(t);
  }

  public DataInputParameter<String> getSource() {
    return source;
  }

  public void setSource(DataInputParameter<String> src) {
    this.source = Args.notNull(src, "src");
  }

  public DataOutputParameter<String> getTarget() {
    return target;
  }

  public void setTarget(DataOutputParameter<String> target) {
    this.target = Args.notNull(target, "target");
  }

}
