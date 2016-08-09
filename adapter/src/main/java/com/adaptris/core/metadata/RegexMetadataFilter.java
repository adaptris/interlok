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
import java.util.Set;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
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
public class RegexMetadataFilter implements MetadataFilter {

  @XStreamImplicit(itemFieldName = "include-pattern")
  @NotNull
  @AutoPopulated
  private List<String> includePatterns;
  @XStreamImplicit(itemFieldName = "exclude-pattern")
  @NotNull
  @AutoPopulated
  private List<String> excludePatterns;

  private transient List<Pattern> incPatterns;
  private transient List<Pattern> excPatterns;

  public RegexMetadataFilter() {
    setIncludePatterns(new ArrayList<String>());
    setExcludePatterns(new ArrayList<String>());
    incPatterns = new ArrayList<Pattern>();
    excPatterns = new ArrayList<Pattern>();
  }

  @Override
  public MetadataCollection filter(AdaptrisMessage message) {
    return filter(message.getMetadata());
  }

  @Override
  public MetadataCollection filter(Set<MetadataElement> original) {
    return filter(new MetadataCollection(original));
  }

  @Override
  public MetadataCollection filter(MetadataCollection original) {
    MetadataCollection included = include((MetadataCollection) original.clone());
    return exclude(included);
  }

  // Lewin - This is a pretty naive way to initialise, but the filter doesn't have a lifecycle so we can't
  // really do much else; as B B king would say, we're paying the cost to be the boss.
  private void initialisePatterns() {
    if (includePatterns.size() != incPatterns.size()) {
      incPatterns.clear();
      for (String regex : getIncludePatterns()) {
        incPatterns.add(Pattern.compile(regex));
      }
    }
    if (excludePatterns.size() != excPatterns.size()) {
      excPatterns.clear();
      for (String regex : getExcludePatterns()) {
        excPatterns.add(Pattern.compile(regex));
      }
    }
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
    for (MetadataElement element : metadataCollection) {
      for (Pattern pattern : excPatterns) {
        if (pattern.matcher(element.getKey()).find()) {
          toBeRemoved.add(element);
          break;
        }
      }
    }
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
    for (MetadataElement element : metadataCollection) {
      for (Pattern pattern : incPatterns) {
        if (pattern.matcher(element.getKey()).find()) {
          result.add(element);
          break;
        }
      }
    }
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
}
