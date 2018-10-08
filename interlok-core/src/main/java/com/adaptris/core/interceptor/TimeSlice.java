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

import java.util.Date;

/**
 * A Timeslice used by {@link ThrottlingInterceptor}.
 * 
 * @config time-slice
 * @author amcgrath
 */
public class TimeSlice {

	private long endMillis;
	private int totalMessageCount;

	public TimeSlice() {
		super();
	}

	public TimeSlice(long end, int count) {
		setEndMillis(end);
		setTotalMessageCount(count);
	}

	public long getEndMillis() {
		return endMillis;
	}

	public void setEndMillis(long endMillis) {
		this.endMillis = endMillis;
	}

	public int getTotalMessageCount() {
		return totalMessageCount;
	}

	public void setTotalMessageCount(int messageCount) {
		totalMessageCount = messageCount;
	}

	@Override
  public String toString() {
    return "[Count-" + getTotalMessageCount() + "][End-(" + new Date(getEndMillis()) + ")]";
	}
}
