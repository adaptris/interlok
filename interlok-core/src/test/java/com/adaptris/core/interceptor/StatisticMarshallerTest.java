package com.adaptris.core.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.Before;
import org.junit.Test;

public class StatisticMarshallerTest {
  
  private StatisticMarshaller statisticMarshaller;
  
  @Before
  public void setUp() throws Exception {
    statisticMarshaller = new StatisticMarshaller();
  }
  
  @Test
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

  @Test
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

  @Test
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

  @Test
  public void testMarshallWrongObjectNoError() throws Exception {
    assertNull(statisticMarshaller.marshal(new Object()));
  }
  
}
