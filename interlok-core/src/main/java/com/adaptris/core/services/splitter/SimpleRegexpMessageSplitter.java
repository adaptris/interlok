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

package com.adaptris.core.services.splitter;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Class which splits batched files in a singel <code>AdaptrisMessage</code> into individual ones based on a regular expression
 * match. It will optionally group records according to some common element, for example a Purchase Order number.
 * </p>
 * 
 * @config simple-regexp-message-splitter
 */
@XStreamAlias("simple-regexp-message-splitter")
@DisplayOrder(order = {"splitPattern", "copyMetadata", "copyObjectMetadata", "matchPattern", "compareToPreviousMatch",
    "ignoreFirstSubMessage"})
public class SimpleRegexpMessageSplitter extends StringPayloadSplitter {
  @NotNull
  @NotBlank
  private String splitPattern;
  @AdvancedConfig
  private String matchPattern;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean compareToPreviousMatch;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean ignoreFirstSubMessage;

  private transient Pattern compiledSplitPattern;
  private transient Pattern compiledMatchPattern;

  public SimpleRegexpMessageSplitter() {

  }

  public SimpleRegexpMessageSplitter(String pattern) {
    this();
    setSplitPattern(pattern);
  }

  @Override
  protected ArrayList<String> split(String messagePayload) throws Exception {
    String batch = messagePayload;
    compiledSplitPattern = compile(compiledSplitPattern, splitPattern);
    Matcher splitMatcher = compiledSplitPattern.matcher(batch);
    Matcher compareMatcher = null;
    if (compareToPreviousMatch()) {
      compiledMatchPattern = compile(compiledMatchPattern, matchPattern);
      compareMatcher = compiledMatchPattern.matcher(batch);
    }

    ArrayList<String> splitMessages = new ArrayList();
    int splitStart = 0;
    int splitEnd = 0;
    StringBuffer currentSplitMsg = new StringBuffer();
    String currentMatch = null;
    if (compareToPreviousMatch()) {
      try {
        // do not check for a match first - we want to throw an exception if
        // no match found.
        compareMatcher.find(); // lgtm
        currentMatch = compareMatcher.group(1);
      }
      catch (Exception e) {
        throw new Exception("Could not match record comparator [" + e.getMessage() + "]", e);
      }
    }

    while (splitMatcher.find(splitEnd)) {
      splitEnd = splitMatcher.end();
      String thisRecord = batch.substring(splitStart, splitEnd);
      if (compareToPreviousMatch()) {
        compareMatcher = compiledMatchPattern.matcher(thisRecord);
        // We may get an empty line, in which case the compare.start() and
        // compare.end() methods will throw an IllegalStateException
        String newMatch = "";
        if (compareMatcher.find()) {
          newMatch = compareMatcher.group(1);
        }
        if (currentMatch.equals(newMatch)) { // lgtm
          // Still in the same message
          currentSplitMsg.append(thisRecord);
        }
        else {
          // The current thisRecord value is actually the start of the next
          // message, so we should store the last record and start again
          // with this one.
          splitMessages.add(currentSplitMsg.toString()); // ********
          currentSplitMsg.setLength(0);
          currentSplitMsg.append(thisRecord);
          currentMatch = newMatch;
        }
      }
      else {
        splitMessages.add(thisRecord);
      }
      splitStart = splitEnd;
    }
    // last message - might be an empty String
    String thisRecord = batch.substring(splitStart);
    if (compareToPreviousMatch()) {
      compareMatcher = compiledMatchPattern.matcher(thisRecord);
      // We may get an empty line, in which case the compare.start() and
      // compare.end() methods will throw an IllegalStateException
      String newMatch = "";
      if (compareMatcher.find()) {
        newMatch = compareMatcher.group(1);
      }
      if (currentMatch.equals(newMatch)) { // lgtm
        // Still in the same message
        currentSplitMsg.append(thisRecord);
        splitMessages.add(currentSplitMsg.toString());
      }
      else {
        // The current thisRecord value is actually the start of the next
        // message, so we should store the last record and start again
        // with this one.
        splitMessages.add(currentSplitMsg.toString());
        currentSplitMsg.setLength(0);
        // Must be a single line record - write it out (unless empty)
        if (thisRecord.trim().length() > 0) {
          splitMessages.add(thisRecord);
        }
      }
    }
    else {
      // Must be a single line record - write it out (unless empty)
      if (thisRecord.trim().length() > 0) {
        splitMessages.add(thisRecord);
      }
    }
    if (ignoreFirstSubMessage()) {
      splitMessages.remove(0);
    }
    return splitMessages;
  }

  private static Pattern compile(Pattern existing, String pattern) throws Exception {
    if (existing != null) {
      return existing;
    }
    return Pattern.compile(pattern);
  }

  /**
   * <p>
   * Sets the regular expression to split on. Examples include:
   * </p>
   * <ul>
   * <li>\n - to split on a carriage return</li>
   * <li>.{10}0001 - to split each time a record '0001' is identified (starts 10 characters in)</li>
   * </ul>
   *
   * @param pattern the split pattern
   */
  public void setSplitPattern(String pattern) {
    splitPattern = pattern;
  }

  /**
   * <p>
   * Gets the regular expression to split on.
   *
   * @return the split pattern
   */
  public String getSplitPattern() {
    return splitPattern;
  }

  /**
   * <p>
   * Sets the regular expression to group records on. Must contain a backreference which will contain the actual text to be
   * compared. Examples include:
   * </p>
   * <ul>
   * <li>[^,]+,[^,]+,([^,]+) - extracts the 3rd field in a CSV file</li>
   * </ul>
   *
   * @param pattern the match pattern
   */
  public void setMatchPattern(String pattern) {
    matchPattern = pattern;
  }

  /**
   * <p>
   * Gets the regular expression to group records on.
   * </p>
   *
   * @return the match pattern
   */
  public String getMatchPattern() {
    return matchPattern;
  }

  /**
   * Specify whether to group split records or not
   * <p>
   * Records identified by the split pattern which would normally be output into separate messages will instead be grouped into
   * compound messages if the value extracted by the specified match pattern is the same across them. The splitter will only match
   * against the record which was separated out immediately before this one.
   * </p>
   *
   * @param b whether to group split records or not, default false
   */
  public void setCompareToPreviousMatch(Boolean b) {
    compareToPreviousMatch = b;
  }

  /**
   * <p>
   * Returns whether this splitter will group matched records together.
   * </p>
   *
   * @return hether this splitter will group matched records together, default false
   */
  public Boolean getCompareToPreviousMatch() {
    return compareToPreviousMatch;
  }

  public boolean compareToPreviousMatch() {
    return BooleanUtils.toBooleanDefaultIfNull(getCompareToPreviousMatch(), false);
  }

  /**
   * <p>
   * Specifies whether to omit the first record parsed, to avoid creating empty records if the split pattern matches on the
   * beginning of a new message rather than the end of the old one.
   * </p>
   *
   * @param b whether to omit the first record parsed, default false
   */
  public void setIgnoreFirstSubMessage(Boolean b) {
    ignoreFirstSubMessage = b;
  }

  /**
   * <p>
   * Returns whether this splitter will ignore the first record or not.
   * </p>
   *
   * @return whether this splitter will ignore the first record or not
   */
  public Boolean getIgnoreFirstSubMessage() {
    return ignoreFirstSubMessage;
  }

  public boolean ignoreFirstSubMessage() {
    return BooleanUtils.toBooleanDefaultIfNull(getIgnoreFirstSubMessage(), false);
  }
}
