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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.ServiceLoader;
import com.adaptris.interlok.types.InterlokMessage;
import lombok.AccessLevel;
import lombok.Getter;

public class ExternalResolver {

  @Getter(AccessLevel.PRIVATE)
  private Collection<Resolver> resolverList = new ArrayList<>();

  private static final ExternalResolver IMPL = new ExternalResolver();

  private ExternalResolver() {
    Iterable<Resolver> resolvers = ServiceLoader.load(Resolver.class);
    for (Resolver r : resolvers) {
      resolverList.add(r);
    }
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

  private String tryResolve(String value) {
    Optional<Resolver> impl = resolverList.stream().filter((r) -> r.canHandle(value)).findFirst();
    return impl.map((r) -> r.resolve(value)).orElseGet(() -> value);
  }

  private String tryResolve(String value, InterlokMessage target) {
    Optional<Resolver> impl = resolverList.stream().filter((r) -> r.canHandle(value)).findFirst();
    return impl.map((r) -> r.resolve(value, target)).orElseGet(() -> value);
  }


}
