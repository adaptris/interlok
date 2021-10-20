/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;

/**
 * A simple bean util class that allows us to call setters that have a {@code primitive} param.
 * 
 *
 */
@SuppressWarnings({"rawtypes"})
public abstract class SimpleBeanUtil {
  private static final List PRIMITIVES = Arrays.asList(int.class, Integer.class, Boolean.class, boolean.class, String.class,
      float.class, Float.class, double.class, Double.class,
      long.class, Long.class);
  private static final Map<Class, Objectifier> OBJECTIFIERS;

  static {
    Map<Class, Objectifier> map = new HashMap<>();
    map.put(int.class, Integer::parseInt);
    map.put(Integer.class, Integer::parseInt);
    map.put(Boolean.class, BooleanUtils::toBoolean);
    map.put(boolean.class, BooleanUtils::toBoolean);
    map.put(String.class, (s) -> s);
    map.put(float.class, Float::parseFloat);
    map.put(Float.class, Float::parseFloat);
    map.put(double.class, Double::parseDouble);
    map.put(Double.class, Double::parseDouble);
    map.put(long.class, Long::parseLong);
    map.put(Long.class, Long::parseLong);
    OBJECTIFIERS = Collections.unmodifiableMap(map);
  }

  @FunctionalInterface
  private interface Objectifier {
    Object objectify(String str);
  }

  /**
   * Invoke the setter method on the object.
   * <p>
   * Uses the first match, so overloaded methods may cause unexpected behaviour. Assumes that the {@code String} value can be
   * converted into its corresponding primitive value (long/double/boolean/string/float/int). If it can't a runtime exception is
   * probably going to be thrown.
   * </p>
   * 
   * @param obj the object.
   * @param methodName the method name (e.g. {@code setClientID}, case insensitive match).
   * @param value the value; which will be converted into the appropriate primitive.
   * @return true if the setter was successfully called; false otherwise.
   */
  public static boolean callSetter(Object obj, String methodName, String value) {
    boolean result = false;
    try {
      Method m = getSetterMethod(obj.getClass(), methodName);
      Object param = OBJECTIFIERS.get(m.getParameterTypes()[0]).objectify(value);
      m.invoke(obj, param);
      result = true;
    }
    catch (NullPointerException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ignored) {
    }
    return result;
  }

  private static Method getSetterMethod(Class c, String methodName) {
    Method result = null;
    Method[] methods = c.getMethods();
    for (Method m : methods) {
      String name = m.getName();
      if (name.equalsIgnoreCase(methodName)) {
        Class[] params = m.getParameterTypes();
        if (params.length == 1 && PRIMITIVES.contains(params[0])) {
          result = m;
          break;
        }
      }
    }
    return result;
  }


  /** Return all the vanilla getters as a map keyed by the method name (uppercase).
   *
   * @param c the class
   * @return a Map keyed by string.
   */
  public static Map<String, Method> gettersAsMap(Class c) {
    Map<String, Method> result = new HashMap<>();
    Method[] methods = c.getMethods();
    for (Method m : methods) {
      String name = m.getName();
      if (!name.startsWith("get") || name.equals("getClass") || name.equals("getInstance")
          || m.getParameterTypes().length != 0) {
        continue;
      }
      result.put(name.toUpperCase(), m);
    }
    return result;
  }
}
