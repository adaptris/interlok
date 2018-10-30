package com.adaptris.core.interceptor;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AbstractMarshaller;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.CoreException;
import com.adaptris.core.XStreamMarshaller;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This marshaller is designed for {@link InterceptorStatistic} and is used exclusively with the {@link ProducingStatisticManager}.
 * </p>
 * <p>
 * We convert the {@link InterceptorStatistic} into a {@link SerializableStatistic} and then use the configured XStream marshaller to serialize the object. <br />
 * The defauilt XStream marshaller is for XML, but you can configure the JSON version by setting the property "actual-marshaller".
 * </p>
 * @author aaron
 *
 */
@XStreamAlias("statistic-marshaller")
@AdapterComponent
@ComponentProfile(summary = "Serializer specifically used to create a human readable representation of a metric timeslice.", tag = "marshaller")
public class StatisticMarshaller extends AbstractMarshaller {
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());
  
  @AutoPopulated
  @AdvancedConfig
  private AdaptrisMarshaller actualMarshaller;

  public StatisticMarshaller() {
    this.setActualMarshaller(new XStreamMarshaller());
  }
  
  @Override
  public String marshal(Object obj) throws CoreException {
    SerializableStatistic serializeStatistic = serializeStatistic(obj);
    if(serializeStatistic != null)
      return this.getActualMarshaller().marshal(serializeStatistic);
    else return null;
  }

  @Override
  public void marshal(Object obj, Writer writer) throws CoreException {
    SerializableStatistic serializeStatistic = serializeStatistic(obj);
    if(serializeStatistic != null)
      this.getActualMarshaller().marshal(serializeStatistic, writer);
  }

  @Override
  public Object unmarshal(Reader reader) throws CoreException {
    return this.getActualMarshaller().unmarshal(reader);
  }

  private SerializableStatistic serializeStatistic(Object obj) throws CoreException {
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
        Map<String, String> mapOfProperties = ((MetadataStatistic) stat).getMetadataStatistics().entrySet().stream().collect(
            Collectors.toMap(
                e -> (String) e.getKey(),
                e -> (String) e.getValue()
        ));
        
        serializableStatistic.setMetadataStatistics(mapOfProperties);
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
