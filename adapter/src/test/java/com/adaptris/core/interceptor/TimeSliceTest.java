package com.adaptris.core.interceptor;

import junit.framework.TestCase;

public class TimeSliceTest extends TestCase {
	
	private TimeSlice timeSlice;
	
	public void setUp() throws Exception {
		timeSlice = new TimeSlice();
	}
	
	public void tearDown() throws Exception {
		
	}
	
	public void testSetAndGetEndMillis() {
		timeSlice.setEndMillis(1000);
		assertEquals(1000, timeSlice.getEndMillis());
	}
	
	public void testSetAndGetTotalMessageCount() {
		timeSlice.setTotalMessageCount(10);
		assertEquals(10, timeSlice.getTotalMessageCount());
	}

}
