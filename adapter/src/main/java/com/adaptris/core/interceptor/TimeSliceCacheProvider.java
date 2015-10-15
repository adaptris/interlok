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

import com.adaptris.core.CoreException;

/**
 * interface for caching timeslices.
 * 
 * @author amcgrath
 */
public interface TimeSliceCacheProvider {

	/**
	 * Once the adapter component starts, any interceptors including any
	 * cache providers will also be started.
	 * @throws CoreException
	 */
	void start() throws CoreException;

	/**
	 * Once the adapter component initializes, any interceptors including any
	 * cache providers will also be initialized.
	 * @throws CoreException
	 */
	void init() throws CoreException;

	/**
	 * Once the adapter component stops, any interceptors including any
	 * cache providers will also be stopped.
	 */
	void stop();

	/**
	 * Will persist the time slice for later retrieval.
	 */
	void update(String cacheName, TimeSlice timeslice);

	/**
	 * Will return the current time slice.
	 * @return TimeSlice
	 */
	TimeSlice get(String cacheName);
}
