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
