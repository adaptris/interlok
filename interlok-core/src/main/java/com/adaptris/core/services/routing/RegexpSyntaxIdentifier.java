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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.conditional.Condition;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Match against a number of configured regular expressions.
 * <p>
 * The regular expressions are the same as those in the <code>java.util.regex</code> package.
 * </p>
 * <p>
 * Since <strong>3.10.0</strong> this class implements {@link Condition} which means that it can be used as part of the conditional
 * services; if used in such a manner, then configuration is contextual, get/setDestination will be ignored (but may still have to
 * be configured due to validation
 * </p>
 * 
 * @config routing-regexp-syntax-identifier
 * @see java.util.regex.Pattern
 * @author sellidge
 */
@XStreamAlias("routing-regexp-syntax-identifier")
@DisplayOrder(order = {"destination", "patterns"})
public class RegexpSyntaxIdentifier extends SyntaxIdentifierImpl {
  private transient List<Pattern> patternList = null;
  private transient boolean initialised = false;

  public RegexpSyntaxIdentifier() {
    super();
    patternList = new ArrayList<Pattern>();
  }

  public RegexpSyntaxIdentifier(List<String> patterns, String dest) {
    this();
    setDestination(dest);
    setPatterns(patterns);
  }

  /**
   * @see SyntaxIdentifier#isThisSyntax(java.lang.String)
   */
  @Override
  public boolean isThisSyntax(String message) throws ServiceException {
    initialise();
    for (Pattern p : patternList) {
      Matcher m = p.matcher(message);
      if (!m.matches()) {
        // Failed to match this pattern as it is an implicit AND
        // condition
        return false;
      }
    }
    return true;
  }

  private void initialise() throws ServiceException {
    if (initialised) {
      return;
    }
    try {
      patternList.clear();
      for (String s : getPatterns()) {
        patternList.add(Pattern.compile(s, Pattern.DOTALL));
      }
      initialised = true;
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
  }
}
