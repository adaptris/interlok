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
