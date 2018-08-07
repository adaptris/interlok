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

package com.adaptris.core.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Regular Expression based implementation of {@link MetadataFilter}
 * <p>
 * Each of the configured <code>include</code> or <code>exclude</code> elements is considered a regular expression as defined by
 * {@link Pattern}. These are then matched against the key portion of the {@link MetadataElement}.
 * </p>
 * <p>
 * All the includes are processed first to generate a subset of metadata that is then used to process any exclusions. The
 * <code>include</code> and <code>excludes</code> should be in <code>String</code> regular expression format. If you do not specify
 * any includes, then this implicitly means all elements are included. If you do not specify any excludes then nothing is excluded
 * from the inclusion subset.
 * </p>
 * <p>
 * Typically, you shouldn't need both <code>includes</code> and <code>excludes</code> because your skill with regular expressions
 * mean that you can handle everything you need in either an <code>includes</code> or an <code>excludes</code> block. It is
 * processed this way for completeness.
 * </p>
 * 
 * @config regex-metadata-filter
 * @author amcgrath
 * 
 */
@XStreamAlias("regex-metadata-filter")
@DisplayOrder(order = {"includePatterns", "excludePatterns"})
public class RegexMetadataFilter extends MetadataFilterImpl {

  @XStreamImplicit(itemFieldName = "include-pattern")
  @NotNull
  @AutoPopulated
  private List<String> includePatterns;
  @XStreamImplicit(itemFieldName = "exclude-pattern")
  @NotNull
  @AutoPopulated
  private List<String> excludePatterns;

  private transient List<Pattern> patternIncludes;
  private transient List<Pattern> patternExcludes;

  public RegexMetadataFilter() {
    setIncludePatterns(new ArrayList<String>());
    setExcludePatterns(new ArrayList<String>());
    patternIncludes = new ArrayList<Pattern>();
    patternExcludes = new ArrayList<Pattern>();
  }

  @Override
  public MetadataCollection filter(MetadataCollection original) {
    MetadataCollection included = include((MetadataCollection) original.clone());
    return exclude(included);
  }

  private void initialisePatterns() {
    patternIncludes = validatePatterns(getIncludePatterns(), patternIncludes);
    patternExcludes = validatePatterns(getExcludePatterns(), patternExcludes);
  }

  /**
   * Take a collection of metadata items and filter that collection based on the list of excluding
   * regular expression patterns.
   *
   * @param metadataCollection the <code>MetadataCollection</code>
   * @return A new modified collection.
   */
  private MetadataCollection exclude(MetadataCollection metadataCollection) {
    if (getExcludePatterns().size() == 0) {
      return metadataCollection;
    }
    initialisePatterns();
    MetadataCollection toBeRemoved = new MetadataCollection();
    toBeRemoved.addAll(metadataCollection.parallelStream().filter(e -> matches(e, patternExcludes)).collect(Collectors.toList()));
    metadataCollection.removeAll(toBeRemoved);
    return metadataCollection;
  }

  /**
   * Take a collection of metadata items and filter that collection based on the list of including
   * regular expression patterns.
   *
   * @param metadataCollection the <code>MetadataCollection</code>
   * @return A new modified collection.
   */
  private MetadataCollection include(MetadataCollection metadataCollection) {
    if (getIncludePatterns().size() == 0) {
      return metadataCollection;
    }
    initialisePatterns();
    MetadataCollection result = new MetadataCollection();
    result.addAll(metadataCollection.parallelStream().filter(e -> matches(e, patternIncludes)).collect(Collectors.toList()));
    return result;
  }

  public List<String> getIncludePatterns() {
    return includePatterns;
  }

  public void setIncludePatterns(List<String> includes) {
    this.includePatterns = includes;
  }

  public List<String> getExcludePatterns() {
    return excludePatterns;
  }

  public void setExcludePatterns(List<String> excludes) {
    this.excludePatterns = excludes;
  }

  public void addIncludePattern(String pattern) {
    getIncludePatterns().add(pattern);
  }

  public void addExcludePattern(String pattern) {
    getExcludePatterns().add(pattern);
  }

  public RegexMetadataFilter withExcludePatterns(String... patterns) {
    for (String p : patterns) {
      this.addExcludePattern(p);
    }
    return this;
  }

  public RegexMetadataFilter withIncludePatterns(String... patterns) {
    for (String p : patterns) {
      this.addIncludePattern(p);
    }
    return this;
  }

  public static List<Pattern> validatePatterns(List<String> patternSpec, List<Pattern> existingPatterns) {
    if (existingPatterns.size() != patternSpec.size()) {
      existingPatterns.clear();
      patternSpec.forEach(e -> {
        existingPatterns.add(Pattern.compile(e));
      });
    }
    return existingPatterns;
  }

  public static boolean matches(MetadataElement element, List<Pattern> patterns) {
    boolean result = false;
    Optional<Matcher> found = patterns.stream().map(pattern -> {
      return pattern.matcher(element.getKey());
    }).filter(Matcher::find).findAny();
    return found.isPresent();
  }

}
