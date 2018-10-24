package com.adaptris.core.interceptor;

import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AbstractMarshaller;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.CoreException;
import com.adaptris.core.XStreamMarshaller;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("statistic-marshaller")
public class StatisticMarshaller extends AbstractMarshaller {
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());
  
  private AdaptrisMarshaller actualMarshaller;

  public StatisticMarshaller() {
    actualMarshaller = new XStreamMarshaller();
  }
  
  @Override
  public String marshal(Object obj) throws CoreException {
    SerializableStatistic serilizeStatistic = serilizeStatistic(obj);
    return actualMarshaller.marshal(serilizeStatistic);
  }

  @Override
  public void marshal(Object obj, Writer writer) throws CoreException {
    SerializableStatistic serilizeStatistic = serilizeStatistic(obj);
    actualMarshaller.marshal(serilizeStatistic, writer);
  }

  @Override
  public Object unmarshal(Reader reader) throws CoreException {
    return actualMarshaller.unmarshal(reader);
  }

  private SerializableStatistic serilizeStatistic(Object obj) throws CoreException {
    if(obj instanceof InterceptorStatistic) {
      
      InterceptorStatistic stat = (InterceptorStatistic) obj;
      SerializableStatistic serializableStatistic = new SerializableStatistic();
      serializableStatistic.setStartMillis(stat.getStartMillis());
      serializableStatistic.setEndMillis(stat.getEndMillis());
      
      if(stat instanceof MessageStatistic) {
        serializableStatistic.setTotalMessageCount(((MessageStatistic) stat).getTotalMessageCount());
        serializableStatistic.setTotalMessageErrorCount(((MessageStatistic) stat).getTotalMessageErrorCount());
        serializableStatistic.setTotalMessageSize(((MessageStatistic) stat).getTotalMessageSize());
      } else if (stat instanceof MetadataStatistic) {
        serializableStatistic.setMetadataStatistics(((MetadataStatistic) stat).getMetadataStatistics());
      }
      
      return serializableStatistic;
      
    } else
      log.warn("Object is not a statistic object, therefore skipping serialization.");
    return null;
  }

  public AdaptrisMarshaller getActualMarshaller() {
    return actualMarshaller;
  }

  public void setActualMarshaller(AdaptrisMarshaller actualMarshaller) {
    this.actualMarshaller = actualMarshaller;
  }
}
