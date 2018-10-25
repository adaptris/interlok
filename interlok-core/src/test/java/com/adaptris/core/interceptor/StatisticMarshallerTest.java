package com.adaptris.core.interceptor;

import java.io.StringWriter;
import java.io.Writer;

import junit.framework.TestCase;

public class StatisticMarshallerTest extends TestCase {
  
  private StatisticMarshaller statisticMarshaller;
  
  public void setUp() throws Exception {
    statisticMarshaller = new StatisticMarshaller();
  }
  
  public void tearDown() throws Exception {
    
  }
  
  public void testMarshallMessageStats() throws Exception {
    MessageStatistic statistic = new MessageStatistic();
    statistic.setStartMillis(System.currentTimeMillis());
    statistic.setEndMillis(System.currentTimeMillis());
    statistic.setTotalMessageCount(10);
    statistic.setTotalMessageErrorCount(1);
    statistic.setTotalMessageSize(1000);
    
    SerializableStatistic serializableStat = (SerializableStatistic) statisticMarshaller.unmarshal(statisticMarshaller.marshal(statistic));
    
    assertEquals(statistic.getStartMillis(), serializableStat.getStartMillis());
    assertEquals(statistic.getEndMillis(), serializableStat.getEndMillis());
    assertEquals(statistic.getTotalMessageCount(), serializableStat.getTotalMessageCount());
    assertEquals(statistic.getTotalMessageErrorCount(), serializableStat.getTotalMessageErrorCount());
    assertEquals(statistic.getTotalMessageSize(), serializableStat.getTotalMessageSize());
  }
  
  public void testMarshallMetadataStats() throws Exception {
    MetadataStatistic statistic = new MetadataStatistic();
    statistic.setStartMillis(System.currentTimeMillis());
    statistic.setEndMillis(System.currentTimeMillis());
    statistic.putValue("myKey", 1);
    
    SerializableStatistic serializableStat = (SerializableStatistic) statisticMarshaller.unmarshal(statisticMarshaller.marshal(statistic));
    
    assertEquals(statistic.getStartMillis(), serializableStat.getStartMillis());
    assertEquals(statistic.getEndMillis(), serializableStat.getEndMillis());
    assertEquals(serializableStat.getMetadataStatistics().get("myKey"), "1");
  }
  
  public void testMarshallMetadataStatsWithWriter() throws Exception {
    MetadataStatistic statistic = new MetadataStatistic();
    statistic.setStartMillis(System.currentTimeMillis());
    statistic.setEndMillis(System.currentTimeMillis());
    statistic.putValue("myKey", 1);
    
    Writer writer = new StringWriter();
    statisticMarshaller.marshal(statistic, writer);
    SerializableStatistic serializableStat = (SerializableStatistic) statisticMarshaller.unmarshal(writer.toString());
    
    assertEquals(statistic.getStartMillis(), serializableStat.getStartMillis());
    assertEquals(statistic.getEndMillis(), serializableStat.getEndMillis());
    assertEquals(serializableStat.getMetadataStatistics().get("myKey"), "1");
  }

  public void testMarshallWrongObjectNoError() throws Exception {
    assertNull(statisticMarshaller.marshal(new Object()));
  }
  
}
