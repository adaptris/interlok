package com.adaptris.interlok.util;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.BooleanUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class FileFilterBuilder {

  // Since we declare commons-io in our dependency tree; we can just use the class.getCanonicalName()
  // public static final String DEFAULT_FILE_FILTER_IMP = "org.apache.commons.io.filefilter.RegexFileFilter";
  public static final String DEFAULT_FILE_FILTER_IMP = RegexFileFilter.class.getCanonicalName();

  protected static final Map<Class, Function<String, Object>> SUPPORTED_CNST_ARGS;

  static {
    Map<Class, Function<String, Object>> map = new LinkedHashMap<>(12);
    map.put(String.class, (s) -> s);
    map.put(int.class, (s) -> Integer.parseInt(s));
    map.put(long.class, (s) -> Long.parseLong(s));
    map.put(boolean.class, (s) -> BooleanUtils.toBoolean(s));
    map.put(float.class, (s) -> Float.parseFloat(s));
    map.put(double.class, (s) -> Double.parseDouble(s));

    map.put(Integer.class, (s) -> Integer.parseInt(s));
    map.put(Long.class, (s) -> Long.parseLong(s));
    map.put(Boolean.class, (s) -> BooleanUtils.toBoolean(s));
    map.put(Float.class, (s) -> Float.parseFloat(s));
    map.put(Double.class, (s)-> Double.parseDouble(s));
    SUPPORTED_CNST_ARGS = Collections.unmodifiableMap(map);
  }

  /**
   * Build a file filter implementation based on the parameters
   * <p>
   * <ul>
   * <li>If filterExpression is empty and filterImpl is the default
   * '{@value #DEFAULT_FILE_FILTER_IMP}' then a functional filter that accepts all inputs is
   * returned</li>
   * <li>If filterExpression is empty and filterImpl is not the default, then we attempt to use the
   * default no-args constructor using {@code Class.forName(filterImpl).newInstance()}. If there
   * isn't an accessible no-args constructor, then a RuntimeException is thrown.</li>
   * <li>If filterExpression is not empty, then we attempt to convert the the filterExpression into
   * something that is supported by the declared constructors from the class. If conversion fails,
   * then a RuntimeException will be thrown.</li>
   * <li>If filterImpl is empty, then a RuntimeException exception is thrown</li>
   * </ul>
   *
   * @param filterExpression the filter expression suitable for the implementation in question
   * @param filterImpl the class implementing {@link FileFilter}.
   * @return a FileFilter implementation (hopefully never null).
   */
  public static FileFilter build(String filterExpression, String filterImpl) {
    FileFilter result = null;
    try {
      if (isDefault(filterExpression, filterImpl)) {
        result = (file) -> true;
      } else {
        result = build(filterExpression, Class.forName(Args.notBlank(filterImpl, "filterImpl")));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  private static FileFilter build(String filterExpr, Class filterClass) throws Exception {
    FileFilter result = null;
    if (isEmpty(filterExpr)) {
      result = (FileFilter) filterClass.newInstance();
    } else {
      for (Map.Entry<Class, Function<String, Object>> converter : SUPPORTED_CNST_ARGS.entrySet()) {
        if (hasConstructor(converter.getKey(), filterClass)) {
          // It has a constructor, we try it, bearing in mind that the converter
          // may fail to convert so
          // If someone passes in "a-string" with SizeFileFilter; then we will
          // fail to convert to a long and throw an exception at this point.
          Object constructorArg = converter.getValue().apply(filterExpr);
          Constructor constructor =
              filterClass.getDeclaredConstructor(new Class[] {converter.getKey()});
          result = (FileFilter) constructor.newInstance(new Object[] {constructorArg});
          break;
        }
      }
      if (result == null) {
        throw new InstantiationException(
            filterClass.getCanonicalName() + " could not be created with [" + filterExpr + "]");
      }
    }
    return result;
  }

  private static boolean isDefault(String filterExp, String filterClass) {
    return BooleanUtils.and(
        new boolean[] {isEmpty(filterExp), DEFAULT_FILE_FILTER_IMP.equalsIgnoreCase(filterClass)});
  }

  private static boolean hasConstructor(Class arg, Class filterClass) {
    try {
      filterClass.getDeclaredConstructor(new Class[] {arg});
      return true;
    } catch (Exception e) {
      return false;
    }
  }

}
