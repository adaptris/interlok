/*
 * Copyright Adaptris Ltd.
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
package com.adaptris.core.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.security.exc.PasswordException;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

public abstract class PasswordMetadataFilter extends MetadataFilterImpl {

  @NotNull
  @AutoPopulated
  @XStreamImplicit(itemFieldName = "password-pattern")
  private List<String> passwordPatterns;
  private transient List<Pattern> passwordPatternMatchers;

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  public PasswordMetadataFilter() {
    setPasswordPatterns(new ArrayList<>());
    passwordPatternMatchers = new ArrayList<>();
  }

  @Override
  public MetadataCollection filter(MetadataCollection original) {
    MetadataCollection result = new MetadataCollection();
    result.addAll(original.stream().map(e -> {
      return matches(e) ? modify(e) : e;
    }).collect(Collectors.toList()));
    return result;
  }

  private MetadataElement modify(MetadataElement e) {
    try {
      return handlePassword(e);
    } catch (PasswordException exc) {
      log.error("Failed to encode/decode password stored against key [{}]", e.getKey());
      throw wrapException(exc);
    }
  }

  protected abstract MetadataElement handlePassword(MetadataElement e) throws PasswordException;

  private boolean matches(MetadataElement element) {
    boolean result = false;
    initialisePatterns();
    Optional<Matcher> found = passwordPatternMatchers.stream().map(pattern -> {
      return pattern.matcher(element.getKey());
    }).filter(Matcher::matches).findAny();
    return found.isPresent();
  }

  private void initialisePatterns() {
    if (passwordPatterns.size() != passwordPatternMatchers.size()) {
      passwordPatternMatchers.clear();
      for (String regex : getPasswordPatterns()) {
        passwordPatternMatchers.add(Pattern.compile(regex));
      }
    }
  }

  protected static RuntimeException wrapException(PasswordException e) {
    return new PasswordRuntimeException(e);
  }
  public List<String> getPasswordPatterns() {
    return passwordPatterns;
  }

  public void setPasswordPatterns(List<String> patterns) {
    this.passwordPatterns = patterns;
  }

  public <T extends PasswordMetadataFilter> T withPatterns(String... patterns) {
    ArrayList<String> list = new ArrayList<>();
    list.addAll(Arrays.asList(patterns));
    setPasswordPatterns(list);
    return (T) this;
  }


  private static class PasswordRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 2018080601;

    public PasswordRuntimeException(PasswordException e) {
      super(e);
    }

  }
}
