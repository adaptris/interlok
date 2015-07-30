package com.adaptris.core.jdbc;

public class JdbcParameterUtils {
  
  public static String objectToString(Object object) {
    if(object != null) {
      if(object instanceof String)
        return (String) object;
      else
        return object.toString();
    } else
      return null;
  }

}
