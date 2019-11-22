package com.adaptris.interlok.util;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileFilterBuilder {

  private static transient Logger log = LoggerFactory.getLogger(FileFilterBuilder.class);
  
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

  public static FileFilter build(String filterExpression, String filterImpl) {
    FileFilter result = null;
    try {
      if (isEmpty(filterExpression)) {
        result = (file) -> true;
      } else {
        Class filterClass = Class.forName(filterImpl);
        for (Map.Entry<Class, Function<String, Object>> converter : SUPPORTED_CNST_ARGS.entrySet()) {
          if (hasConstructor(converter.getKey(), filterClass)) {
            // It has a constructor, we try it, bearing in mind that the converter
            // may fail to convert so
            // If someone passes in "a-string" with SizeFileFilter; then we will
            // fail to convert to a long and throw an exception at this point.
            Object constructorArg = converter.getValue().apply(filterExpression);
            Constructor constructor = filterClass.getDeclaredConstructor(new Class[] {converter.getKey()});
            result = (FileFilter) constructor.newInstance(new Object[] {constructorArg});
            break;
          }
        }
      }
      if (result == null) {
        throw new InstantiationException(filterImpl + " could not be created with [" + filterExpression + "]");
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
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
