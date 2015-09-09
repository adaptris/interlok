package com.adaptris.mail;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import org.apache.oro.text.GlobCompiler;
import org.apache.oro.text.awk.AwkCompiler;
import org.apache.oro.text.awk.AwkMatcher;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * Factory that creates ORO Pattern Compilers and Matchers for use with
 * MailboxClient.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
final class RegExpFactory {
  private static final Class[] COMPILERS =
  {
      AwkCompiler.class, GlobCompiler.class, Perl5Compiler.class
  };

  private static final Class[] MATCHERS =
  {
      AwkMatcher.class, Perl5Matcher.class, Perl5Matcher.class
  };

  private static final String[] COMPILER_MNENOMICS =
  {
      AwkCompiler.class.getName().toUpperCase(), "AWK",
      GlobCompiler.class.getName().toUpperCase(), "GLOB",
      Perl5Compiler.class.getName().toUpperCase(), "PERL5"
  };

  private static final Class[] COMPILER_MNENOMIC_CLASSES =
  {
      AwkCompiler.class, AwkCompiler.class, GlobCompiler.class,
      GlobCompiler.class, Perl5Compiler.class, Perl5Compiler.class
  };

  private static final Map COMPILER_MAP;
  private static final Map MATCHER_MAP;

  static {
    Map m = new Hashtable();
    for (int i = 0; i < COMPILER_MNENOMICS.length; i++) {
      m.put(COMPILER_MNENOMICS[i], COMPILER_MNENOMIC_CLASSES[i]);
    }
    COMPILER_MAP = Collections.unmodifiableMap(m);
    m = new Hashtable();
    for (int i = 0; i < COMPILERS.length; i++) {
      m.put(COMPILERS[i], MATCHERS[i]);
    }
    MATCHER_MAP = Collections.unmodifiableMap(m);
  }

  private RegExpFactory() {
  }

  static PatternCompiler getCompiler(String imp) throws MailException {
    PatternCompiler result = null;
    if (imp == null) {
      return getCompiler(GlobCompiler.class.getName());
    }
    Class clazz = (Class) COMPILER_MAP.get(imp.toUpperCase());
    if (clazz == null) {
      throw new MailException("Unsupported regexp handler " + imp);
    }
    try {
      result = (PatternCompiler) clazz.newInstance();
    }
    catch (Exception e) {
      throw new MailException("Unsupported regexp handler " + imp, e);
    }
    return result;
  }

  static PatternMatcher getMatcher(PatternCompiler p) throws MailException {
    PatternMatcher result = null;
    if (p == null) {
      return getMatcher(new GlobCompiler());
    }
    Class clazz = (Class) MATCHER_MAP.get(p.getClass());
    if (clazz == null) {
      throw new MailException("Unsupported regexp matcher " + p.getClass());
    }
    try {
      result = (PatternMatcher) clazz.newInstance();
    }
    catch (Exception e) {
      throw new MailException("Unsupported regexp matcher " + p.getClass(), e);
    }
    return result;

  }
}
