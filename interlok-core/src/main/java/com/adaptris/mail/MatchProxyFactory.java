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

package com.adaptris.mail;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MatchProxyFactory {

  private static transient Logger log = LoggerFactory.getLogger(MatchProxy.class);
  public static final String DEFAULT_REGEXP_STYLE = "Regex";

  protected enum ProxyBuilder {
    JavaUtilSimple(DEFAULT_REGEXP_STYLE) {
      @Override
      MatchProxy build(String expression) throws Exception {
        return new JavaMatchProxy(expression);
      }

    },
    JavaUtilClass("java.util.regex.Pattern") {
      @Override
      MatchProxy build(String expression) throws Exception {
        return new JavaMatchProxy(expression);
      }

    };

    String myType;

    private ProxyBuilder(String t) {
      myType = t;
    }

    boolean matches(String type) {
      return myType.equalsIgnoreCase(type);
    }

    abstract MatchProxy build(String expression) throws Exception;
  }

  static MatchProxy create(String regularExpressionType, String expression) throws Exception {
    String type = StringUtils.defaultIfBlank(regularExpressionType, DEFAULT_REGEXP_STYLE);
    if (expression == null) {
      return null;
    }
    for (ProxyBuilder c : ProxyBuilder.values()) {
      if (c.matches(type)) {
        return c.build(expression);
      }
    }
    throw new IllegalArgumentException(regularExpressionType + " not supported");
  }

}
