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

import javax.validation.constraints.NotBlank;
import com.adaptris.core.util.Args;

public abstract class SyntaxIdentifierBase implements SyntaxIdentifier {
  @NotBlank
  private String destination = null;

  public SyntaxIdentifierBase() {
  }

  /**
   *  @see SyntaxIdentifier#setDestination(java.lang.String)
   */
  @Override
  public void setDestination(String dest) {
    destination = Args.notBlank(dest, "destination");
  }

  @Override
  public String getDestination() {
    return destination;
  }
}
