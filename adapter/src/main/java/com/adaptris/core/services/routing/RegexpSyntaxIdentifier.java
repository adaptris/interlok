/*
 * $RCSfile: RegexpSyntaxIdentifier.java,v $
 * $Revision: 1.7 $
 * $Date: 2009/01/28 17:18:11 $
 * $Author: lchan $
 */
package com.adaptris.core.services.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Match against a number of configured regular expressions.
 * <p>
 * The regular expressions are the same as those in the <code>java.util.regex</code> package.
 * </p>
 * 
 * @config routing-regexp-syntax-identifier
 * @see java.util.regex.Pattern
 * @author sellidge
 * @author $Author: lchan $
 */
@XStreamAlias("routing-regexp-syntax-identifier")
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
