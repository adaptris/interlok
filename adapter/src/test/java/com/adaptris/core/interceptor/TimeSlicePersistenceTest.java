package com.adaptris.core.interceptor;

import java.util.Date;

import junit.framework.TestCase;

public class TimeSlicePersistenceTest extends TestCase {
	
	private TimeSlicePersistence instance;
	
	public void setUp() throws Exception {
		instance = TimeSlicePersistence.getInstance();
	}
	
	public void tearDown() throws Exception {
		instance.clear();
	}
	
	public void testPutCacheNameDoesNotExist() {
		TimeSlice timeSlice = new TimeSlice(new Date().getTime(), 0);
		instance.updateCurrentTimeSlice("default", timeSlice);
		
		assertTrue(instance.getCacheNames().contains("default"));
	}
	
	public void testPutMultipleCacheNames() {
		TimeSlice timeSlice = new TimeSlice(new Date().getTime(), 0);
		instance.updateCurrentTimeSlice("default", timeSlice);
		instance.updateCurrentTimeSlice("default2", timeSlice);
		instance.updateCurrentTimeSlice("default3", timeSlice);
		
		assertTrue(instance.getCacheNames().contains("default"));
		assertTrue(instance.getCacheNames().contains("default2"));
		assertTrue(instance.getCacheNames().contains("default3"));
	}
	
	public void testGetWithMultipleCacheNames() {
		TimeSlice timeSlice1 = new TimeSlice(new Date().getTime(), 0);
		TimeSlice timeSlice2 = new TimeSlice(new Date().getTime(), 1);
		TimeSlice timeSlice3 = new TimeSlice(new Date().getTime(), 2);
		instance.updateCurrentTimeSlice("default", timeSlice1);
		instance.updateCurrentTimeSlice("default2", timeSlice2);
		instance.updateCurrentTimeSlice("default3", timeSlice3);
		
		assertEquals(1, instance.getCurrentTimeSlice("default2").getTotalMessageCount());
		assertEquals(2, instance.getCurrentTimeSlice("default3").getTotalMessageCount());
		assertEquals(0, instance.getCurrentTimeSlice("default").getTotalMessageCount());
	}

	public void testUpdateCache() {
		TimeSlice timeSlice1 = new TimeSlice(new Date().getTime(), 0);
		instance.updateCurrentTimeSlice("default", timeSlice1);
		assertEquals(0, instance.getCurrentTimeSlice("default").getTotalMessageCount());
		
		timeSlice1.setTotalMessageCount(10);
		instance.updateCurrentTimeSlice("default", timeSlice1);
		assertEquals(10, instance.getCurrentTimeSlice("default").getTotalMessageCount());
	}
	
	public void testUpdateWithMultipleCaches() {
		TimeSlice timeSlice1 = new TimeSlice(new Date().getTime(), 0);
		instance.updateCurrentTimeSlice("default", timeSlice1);
		assertEquals(0, instance.getCurrentTimeSlice("default").getTotalMessageCount());
		
		TimeSlice timeSlice2 = new TimeSlice(new Date().getTime(), 0);
		instance.updateCurrentTimeSlice("default2", timeSlice2);
		assertEquals(0, instance.getCurrentTimeSlice("default2").getTotalMessageCount());
		
		timeSlice1.setTotalMessageCount(10);
		instance.updateCurrentTimeSlice("default", timeSlice1);
		assertEquals(10, instance.getCurrentTimeSlice("default").getTotalMessageCount());
		
		timeSlice2.setTotalMessageCount(109);
		instance.updateCurrentTimeSlice("default2", timeSlice2);
		assertEquals(109, instance.getCurrentTimeSlice("default2").getTotalMessageCount());
	}
}
