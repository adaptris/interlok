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

package com.adaptris.core.services.metadata;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairBag;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Integer based value match implementation of MetadataValueMatcher for {@link MetadataValueBranchingService}.
 * <p>
 * This allows a simple integer comparision to determine the branch that should be used. The key portion of the underlying
 * MetadataToServiceIdMappings field is a simple expression that follows the following syntax <code>&lt;operator&gt;&lt;value&gt;</code>
 * where valid operators are
 * <ol>
 * <li>&#60;</li>
 * <li>&#62;</li>
 * <li>&#61;</li>
 * <li>&#60;&#61;</li>
 * <li>&#62;&#61;</li>
 * </ol>
 * Precedence is determined by the natural ASCII sort order (0-9, &#60;, &#61;, &#62; in that order) of the key portion; the first
 * matching condition will be used. e.g. For a given serviceKey which evaluates to <code>20</code>; if you have both &gt;10 and
 * &gt;=20 configured in MetadataToServiceIdMappings, then <code>&gt;10</code> will be the matching expression due to the natural
 * sort order of the two key values.
 * </p>
 * <p>
 * The service key created from the associated metadata-keys is used as the left hand side of the expression
 * </p>
 * 
 * @config integer-value-matcher
 * 
 * @author lchan
 */
@XStreamAlias("integer-value-matcher")
public class IntegerValueMatcher implements MetadataValueMatcher {

  private transient Logger logR = LoggerFactory.getLogger(this.getClass());

  private static String INTEGER_OPERATOR_PATTERN = "^\\s*([\\>\\=\\<]+)\\s*([0-9]+)$";

  private static enum Operator {
    GreaterThan(">") {
      @Override
      public boolean matches(int lhs, int rhs) {
        return lhs > rhs;
      }
    },
    LessThan("<") {
      @Override
      public boolean matches(int lhs, int rhs) {
        return lhs < rhs;
      }
    },
    GreaterThanOrEqualTo(">=") {
      @Override
      public boolean matches(int lhs, int rhs) {
        return lhs >= rhs;
      }
    },
    LessThanOrEqualTo("<=") {
      @Override
      public boolean matches(int lhs, int rhs) {
        return lhs <= rhs;
      }
    },
    Equals("=") {
      @Override
      boolean matches(int lhs, int rhs) {
        return lhs == rhs;
      }
    };
    private String matchKey = null;

    private Operator(String key) {
      this.matchKey = key;
    }

    public String getMatchKey() {
      return matchKey;
    }

    abstract boolean matches(int lhs, int rhs);

  };

  private static final Map<String, Operator> OPERATORS;

  static {
    Map<String, Operator> m = new HashMap<String, Operator>();
    for (Operator o : Operator.values()) {
      m.put(o.getMatchKey(), o);
    }
    OPERATORS = Collections.unmodifiableMap(m);
  }

  public IntegerValueMatcher() {
  }

  public String getNextServiceId(String serviceKey, KeyValuePairBag mappings) {
    String result = null;
    Integer lhs = null;
    Integer rhs = null;
    try {
      lhs = Integer.parseInt(serviceKey);
    }
    catch (NumberFormatException e) {
      logR.trace("[{}] not parseable as an integer", serviceKey);
      return result;
    }

    Pattern matchPattern = Pattern.compile(INTEGER_OPERATOR_PATTERN);
    Set<KeyValuePair> pairs = sort(mappings);
    for (KeyValuePair kvp : pairs) {
      Matcher m = matchPattern.matcher(kvp.getKey());
      if (m.matches()) {
        String operator = m.group(1);
        rhs = Integer.parseInt(m.group(2));
        Operator o = OPERATORS.get(operator);
        if (o == null) {
          logR.trace("[{}] not handled, ignoring", kvp.getKey());
          continue;
        }
        logR.trace("Comparing [{}] against [{}] using {}", lhs, rhs, o.toString());
        if (o.matches(lhs, rhs)) {
          result = kvp.getValue();
          break;
        }
      }
      else {
        logR.trace("Ignoring {}, no match for regular expression [{}]", kvp.getKey(), INTEGER_OPERATOR_PATTERN);
      }
    }
    return result;
  }

  private static Set<KeyValuePair> sort(KeyValuePairBag mappings) {
    TreeSet<KeyValuePair> result = new TreeSet<KeyValuePair>(new NaturalOrderKeyValueComparator());
    result.addAll(mappings.getKeyValuePairs());
    return result;
  }

  private static class NaturalOrderKeyValueComparator implements Comparator<KeyValuePair> {

    public int compare(KeyValuePair kvp1, KeyValuePair kvp2) {
      return kvp1.getKey().compareTo(kvp2.getKey());
    }

  }
}
