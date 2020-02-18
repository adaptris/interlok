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

package com.adaptris.core.services.routing;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

public abstract class SyntaxIdentifierImpl extends SyntaxIdentifierBase {
  @XStreamImplicit(itemFieldName = "pattern")
  @AutoPopulated
  @NotNull
  private List<String> patterns = null;

  public SyntaxIdentifierImpl() {
    patterns = new ArrayList<String>();
  }

  public void addPattern(String pattern) {
    patterns.add(Args.notBlank(pattern, "pattern"));
  }

  public List<String> getPatterns() {
    return patterns;
  }

  public void setPatterns(List<String> l) {
    patterns = Args.notNull(l, "patterns");
  }
}
