package com.adaptris.core.interceptor;

import java.util.List;

import com.adaptris.core.CoreException;


/**
 * Management bean interface for message statistics.
 * 
 * @author amcgrath
 */
public interface MessageMetricsStatisticsMBean extends MetricsMBean {

  /**
   * Get the number of messages for a given timeslice.
   * 
   * @param index the index of the timeslice.
   * @return the number of messages.
   * @deprecated since 3.0.3 use {@link #getStatistics()} instead for efficiency when dealing with remote MBeans
   */
  @Deprecated
	int getNumberOfMessagesForTimeSliceIndex(int index);

  /**
   * Get the total size of messages processed in a given timeslice
   * 
   * @param index the index of the timeslice
   * @return the total size of messages.
   * @deprecated since 3.0.3 use {@link #getStatistics()} instead for efficiency when dealing with remote MBeans.
   */
  @Deprecated
	long getTotalSizeOfMessagesForTimeSliceIndex(int index);

  /**
   * Get the total number of messages that had an error in a given timeslice.
   * 
   * @param index the index of the timeslice
   * @return the total of messages that errored.
   * @deprecated since 3.0.3 use {@link #getStatistics()} instead for efficiency when dealing with remote MBeans.
   */
  @Deprecated
	int getNumberOfErrorMessagesForTimeSliceIndex(int index);


  /**
   * Get a simple string representation of stats.
   *
   * @return a simple string
   */
	String getTotalStringStats();

  /**
   * Get all the statistics hosted by this management bean.
   * 
   * @return a copy of all the statistics.
   * @since 3.0.3
   */
  List<MessageStatistic> getStatistics() throws CoreException;

  /**
   * Returns a view of the portion of this list between the specified {@code fromIndex}, inclusive,
   * and {@code toIndex}, exclusive.
   * <p>
   * Although similar to {@link List#subList(int, int)}; it is designed to return you a copy of the
   * list in question; any changes to the returned list will not be reflected in the underlying
   * list.
   * </p>
   * 
   * @param fromIndex
   * @param toIndex
   * @return a new list containing the statistics.
   * @since 3.0.3
   */
  List<MessageStatistic> getStatistics(int fromIndex, int toIndex) throws CoreException;

}
