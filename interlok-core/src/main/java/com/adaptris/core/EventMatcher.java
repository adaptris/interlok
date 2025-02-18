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

import java.util.function.Function;

/**
 * Matches on an event
 */
public interface EventMatcher {

    boolean matches(Event event);

    enum EventMatchType {
        SOURCE_ID(event -> event.getSourceId()),
        DESTINATION_ID(event -> event.getDestinationId()),
        NAMESPACE(event -> event.getNameSpace()),
        TYPE(event -> event.getClass().getCanonicalName());

        Function<Event, Object> getter;

        EventMatchType(Function<Event, Object> getter) {
            this.getter = getter;
        }

        public Object getProperty(Event event) {
            return getter.apply(event);
        }
    }
}
