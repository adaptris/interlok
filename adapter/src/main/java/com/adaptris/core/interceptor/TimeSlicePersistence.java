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

package com.adaptris.core.interceptor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A singleton that will manage all caches and for each cache will maintain the current time slice.
 * 
 * @author amcgrath
 */
public class TimeSlicePersistence {

	private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
	private static TimeSlicePersistence instance;
	private Map<String, TimeSlice> timeSlices;

	private TimeSlicePersistence() {
		timeSlices = Collections.synchronizedMap(new HashMap<String, TimeSlice>());
	}

	public static synchronized TimeSlicePersistence getInstance() {
		if(instance == null) {
			instance = new TimeSlicePersistence();
		}

		return instance;
	}

	public TimeSlice getCurrentTimeSlice(String cacheName) {
		return timeSlices.get(cacheName);
	}

	public void updateCurrentTimeSlice(String cacheName, TimeSlice timeSlice) {
		timeSlices.put(cacheName, timeSlice);
	}

	/**
	 * Will clear the time slices that have been recorded so far.
	 * Warning: Calling this method will clear the timeslices for ALL caches.
	 * This means all interceptors will be affected.
	 */
	public void clear() {
		timeSlices.clear();
	}

	public Collection<String> getCacheNames() {
		return timeSlices.keySet();
	}
}
