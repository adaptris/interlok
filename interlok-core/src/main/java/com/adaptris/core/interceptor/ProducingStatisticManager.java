package com.adaptris.core.interceptor;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ProduceException;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This statistic manager allows you to configure a marshaller and a stand alone producer.
 * </p>
 * <p>
 * When the produce() method is triggered we will serialize the {@link InterceptorStatistic} with the configured marshaller and then execute the producer with the resulting string payload.
 * </p>
 * <p>
 * If the producer should fail, we simply attempt to restart the stand alone producer and continue.
 * </p>
 * @author aaron
 *
 */
@JacksonXmlRootElement(localName = "producing-statistic-manager")
@XStreamAlias("producing-statistic-manager")
@AdapterComponent
@ComponentProfile(summary = "Statistic manager that allows configuration of a standalone producer for each timeslice when they expire.", tag = "interceptor")
public class ProducingStatisticManager extends BaseStatisticManager {

  private StandaloneProducer producer;
  
  @AutoPopulated
  private AdaptrisMarshaller marshaller;
  
  public ProducingStatisticManager() {
    this.setMarshaller(new StatisticMarshaller());
  }
  
  public ProducingStatisticManager(int maxHistoryCount) {    
    super(maxHistoryCount);
    this.setMarshaller(new StatisticMarshaller());
  }
  
  @Override
  public void produce(InterceptorStatistic interceptorStatistic) throws ProduceException {
    try {
      log.trace("Producing serialized timeslice.");
      String marshalledTimeslice = this.getMarshaller().marshal(interceptorStatistic);
      AdaptrisMessage newMessage = DefaultMessageFactory.getDefaultInstance().newMessage(marshalledTimeslice);
      
      this.getProducer().produce(newMessage);
      
    } catch (CoreException e) {
      try {
        this.restartProducer();
      } catch (CoreException e1) {
        log.error("Failed to restart producer, will try again on next produce.");
      }
      log.error("Failed to produce timeslice.  Restarting producer.", e);
    }
  }
  
  private void restartProducer() throws CoreException {
    LifecycleHelper.stop(getProducer());
    LifecycleHelper.close(getProducer());
    LifecycleHelper.init(getProducer());
    LifecycleHelper.start(getProducer());
  }
  
  @Override
  public void init() throws CoreException {
    LifecycleHelper.init(this.getProducer());
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(this.getProducer());
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(this.getProducer());
  }

  @Override
  public void close() {
    LifecycleHelper.close(this.getProducer());
  }

  public StandaloneProducer getProducer() {
    return producer;
  }

  public void setProducer(StandaloneProducer producer) {
    this.producer = producer;
  }

  public AdaptrisMarshaller getMarshaller() {
    return marshaller;
  }

  public void setMarshaller(AdaptrisMarshaller marshaller) {
    this.marshaller = marshaller;
  }

}
