package com.adaptris.tester.utils;

import com.adaptris.core.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleStringSubstitution {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  private transient boolean strictMode = false;
  private transient boolean verboseMode = false;

  public SimpleStringSubstitution() {
    this(false, false);
  }

  public SimpleStringSubstitution(boolean verbose, boolean strict) {
    strictMode = strict;
    verboseMode = verbose;
  }

  public String doSubstitution(String input, Properties variableSubs, String variablePrefix, String variablePostFix) throws UtilityException{
    Set<String> keySet = variableSubs.stringPropertyNames();
    log.trace("Performing configuration variable substitution");
    for (String key : keySet) {
      String variable = variablePrefix + key + variablePostFix;
      if (verboseMode) {
        log.trace("Replacing {} with {}", variable, variableSubs.getProperty(key));
      }
      input = input.replace(variable, variableSubs.getProperty(key));
    }
    validateSubstitutions(input, variablePrefix, variablePostFix);
    return input;
  }

  private void validateSubstitutions(String input, String variablePrefix, String variablePostFix) throws UtilityException {
    Pattern hasMoreVars = buildPattern(variablePrefix, variablePostFix);
    Matcher matcher = hasMoreVars.matcher(input);
    if (matcher.matches()) {
      String varName = variablePrefix + matcher.group(1) + variablePostFix;
      if (strictMode) {
        throw new UtilityException(varName + " is undefined for variable substitution");
      } else {
        log.warn("{} is undefined for variable substitution", varName);
      }
    }
  }


  private static Pattern buildPattern(String variablePrefix, String variablePostFix) {
    // This should give us : ^.*\Q${\E(\S*)\Q}\E.* which we can run in DOTALL mode.
    // This assumes that no one is going to have ${ my variable name with spaces } (eek).
    String pattern = "^.*" + Pattern.quote(variablePrefix) + "(\\S*)" + Pattern.quote(variablePostFix) + ".*";
    return Pattern.compile(pattern, Pattern.DOTALL);
  }

}
