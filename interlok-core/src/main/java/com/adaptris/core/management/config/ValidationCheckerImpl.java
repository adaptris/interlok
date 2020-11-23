package com.adaptris.core.management.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;
import lombok.NoArgsConstructor;

/**
 * Provides some utilities for javax validation style checking.
 *
 */
@NoArgsConstructor
public abstract class ValidationCheckerImpl extends AdapterConfigurationChecker {
  private static final String PATTERN_SERVICE_COLL_ARRAY = ".*serviceCollection\\[[0-9+]\\].*";
  private static final String PATTERN_WORKFLOW_LIST_ARRAY = ".*workflowList\\[[0-9+]\\].*";
  private static final String PATTERN_CHANNEL_LIST_ARRAY = ".*channelList\\[[0-9+]\\].*";


  private static final List<Pattern> ARRAY_PATTERNS =
      Collections.unmodifiableList(Arrays.asList(Pattern.compile(PATTERN_CHANNEL_LIST_ARRAY),
          Pattern.compile(PATTERN_WORKFLOW_LIST_ARRAY),
          Pattern.compile(PATTERN_SERVICE_COLL_ARRAY)));

  // Since ChannelList & workflowList implements collection, we have
  // the weird situation where a single "issue" can trigger 4 separate warnings.
  protected boolean isListImpl(String path) {
    List<Matcher> matcherList =
        ARRAY_PATTERNS.stream().map((p) -> p.matcher(path)).collect(Collectors.toList());
    return IterableUtils.matchesAny(matcherList, (matcher) -> matcher.matches());
  }
}
