/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.interlok.resolver;

import com.adaptris.interlok.types.InterlokMessage;

import java.util.ServiceLoader;

public class ExternalResolver {

  private ServiceLoader<Resolver> resolvers = null;

  private static final ExternalResolver IMPL = new ExternalResolver();

  private ExternalResolver() {

  }

  public static String resolve(String lookupValue) {
    if (null == lookupValue) {
      return null;
    }
    return IMPL.tryResolve(lookupValue);
  }

  public static String resolve(String value, InterlokMessage target) {
    if (value == null) {
      return null;
    }
    return IMPL.tryResolve(value, target);
  }

  private String tryResolve(String lookupValue) {
    String result = lookupValue;
    if (resolvers == null) {
      resolvers = ServiceLoader.load(Resolver.class);
    }
    for (Resolver impl : resolvers) {
      if (impl.canHandle(lookupValue)) {
        result = impl.resolve(lookupValue);
        break;
      }
    }
    return result;
  }

  private String tryResolve(String value, InterlokMessage target) {
    if (resolvers == null) {
      resolvers = ServiceLoader.load(Resolver.class);
    }
    for (Resolver resolver : resolvers) {
      if (resolver.canHandle(value)) {
        return resolver.resolve(value, target);
      }
    }
    return value;
  }
}
