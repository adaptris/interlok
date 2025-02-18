/*
 * Copyright 2024 Adaptris Ltd.
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

package com.adaptris.core;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Matches based on supplied regex
 */
@XStreamAlias("regex-event-matcher")
@AdapterComponent
@ComponentProfile(summary = "A matcher that uses regex to match on an event's properties", tag = "events")
public class RegexEventMatcher implements EventMatcher {


    private String regex;

    @XStreamImplicit(itemFieldName = "match-type")
    private Set<String> matchTypes;

    private transient Set<EventMatchType> matchTypeEnumSet;

    private transient Pattern compiledRegex;

    public RegexEventMatcher() {
        setRegex("");
        setMatchTypes(new LinkedHashSet<>());
    }

    public RegexEventMatcher(String regex, Set<String> matchTypes) {
        setMatchTypes(matchTypes);
        setRegex(regex);
    }

    public void setRegex(String regex) {
        this.regex = regex;
        this.compiledRegex = Pattern.compile(regex);
    }

    public String getRegex() {
        return regex;
    }

    public Set<String> getMatchTypes() {
        return matchTypes;
    }

    public void setMatchTypes(Set<String> matchTypes) {
        this.matchTypeEnumSet = matchTypes.stream().map(EventMatchType::valueOf).collect(Collectors.toSet());
        this.matchTypes = matchTypes;
    }

    @Override
    public boolean matches(Event event) {
        for (EventMatchType matchType : matchTypeEnumSet) {
            if (compiledRegex.matcher((String) matchType.getProperty(event)).matches()) return true;
        }
        return false;
    }
}
