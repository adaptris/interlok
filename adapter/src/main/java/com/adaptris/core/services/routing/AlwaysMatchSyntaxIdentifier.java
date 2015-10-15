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

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Always match.
 * <p>
 * Always returns true when isThisSyntax() is used.
 * </p>
 * 
 * @config routing-always-match-syntax-identifier
 */
@XStreamAlias("routing-always-match-syntax-identifier")
public class AlwaysMatchSyntaxIdentifier extends SyntaxIdentifierImpl {

  public AlwaysMatchSyntaxIdentifier() {
    super();
  }

  public AlwaysMatchSyntaxIdentifier(String dest) {
    this();
    setDestination(dest);
  }


  /**
   *  @see SyntaxIdentifier#isThisSyntax(java.lang.String)
   */
  @Override
  public boolean isThisSyntax(String message) {
    return true;
  }
}
