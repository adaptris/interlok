package com.adaptris.core.stubs;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ObjectUtils extends org.apache.commons.lang3.ObjectUtils {
  private static final Class[] PRIMITIVE_ARRAY =
  {
      int.class, boolean.class, String.class, float.class, double.class, long.class, Integer.class, Boolean.class, Float.class,
      Double.class, Long.class
  };

  private static final List PRIMITIVES = Arrays.asList(PRIMITIVE_ARRAY);

  public static List<Method> getObjectGetters(Class c) {
    List list = new ArrayList();
    List matches = getGetters(c);
    for (Iterator i = matches.iterator(); i.hasNext();) {
      Method m = (Method) i.next();
      if (PRIMITIVES.contains(m.getReturnType())) {
        continue;
      }
      list.add(m);
    }
    return list;
  }

  public static List<Method> getPrimitiveGetters(Class c) {
    List<Method> list = new ArrayList<Method>();
    List<Method> matches = getGetters(c);
    for (Iterator i = matches.iterator(); i.hasNext();) {
      Method m = (Method) i.next();
      if (!PRIMITIVES.contains(m.getReturnType())) {
        continue;
      }
      list.add(m);
    }
    return list;
  }

  public static List<Method> getGetters(Class c) {
    List<Method> list = new ArrayList<Method>();
    Method[] methods = c.getMethods();
    for (int i = 0; i < methods.length; i++) {
      String name = methods[i].getName();
      if (!name.startsWith("get") || name.equals("getClass") || name.equals("getInstance")
          || methods[i].getParameterTypes().length != 0) {
        continue;
      }
      list.add(methods[i]);
    }
    return list;
  }

  // Ensure that the list of getter has a corresponding setter.
  public static String[] filterGetterWithNoSetter(Class c, List<Method> getters) throws Exception {
    List result = new ArrayList();
    for (Method m : getters) {
      String methodName = m.getName();
      String fieldName = methodName.substring(3);
      String setter = "set" + fieldName;
      try {
        c.getMethod(setter, new Class[]
        {
            m.getReturnType()
        });
        result.add(m.getName());

      } catch (NoSuchMethodException e) {
        ;
      }
    }
    return (String[]) result.toArray(new String[0]);
  }

  public static Object invokeGetter(Object obj, String methodName) throws Exception {
    Method m = obj.getClass().getMethod(methodName, (Class[]) null);
    if (m != null) {
      if (m.getExceptionTypes().length != 0) {
        // It throws an exception. It's not a simple getter and setter
        return null;
      } else {
        return m.invoke(obj, (Object[]) null);
      }
    }
    return null;
  }

  public static String[] asSetters(String... getterMethods) {
    List<String> result = new ArrayList<>();
    for (String s : getterMethods) {
      result.add(s.replaceFirst("get", "set"));
    }
    return result.toArray(new String[0]);
  }

  public static void invokeSetter(Object target, Class clazz, String methodName, String getterMethod, Object param)
      throws Exception {
    Method getter = clazz.getMethod(getterMethod, (Class[]) null);
    Method setter = clazz.getMethod(methodName, new Class[]
    {
        getter.getReturnType()
    });
    setter.invoke(target, param);
  }
}
