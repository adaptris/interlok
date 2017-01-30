package com.adaptris.core;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * A Service instance that references a Service made available via {@link SharedComponentList}.
 * </p>
 * <p>
 * By default the looked-up service is deep cloned before being loaded into your workflows. <br/>
 * You can turn off cloning by simply setting "clone-service=true".
 * </p>
 * 
 * @config shared-service
 * @author amcgrath
 * 
 */
@XStreamAlias("shared-service")
@AdapterComponent
@ComponentProfile(summary = "A Service that refers to another Service configured elsewhere", tag = "service,base")
@DisplayOrder(order = {"lookupName"})
public class SharedService extends SharedComponent implements Service {

  @NotBlank
  private String lookupName;
  @NotNull
  @AutoPopulated
  @InputFieldDefault(value="true")
  private Boolean cloneService;
  
  private transient Service clonedService;
  
  public SharedService() {
  }

  public SharedService(String lookupName) {
    this.setLookupName(lookupName);
  }
  
  private Service getProxiedService() {
    try {
      if (clonedService == null) {
        Service lookedUpService = (Service) triggerJndiLookup(getLookupName());
        if(cloneService())
          clonedService = this.deepClone(lookedUpService);
        else
          clonedService = lookedUpService;
      }
    }
    catch (CoreException e) {
      throw new RuntimeException(e);
    }
    return clonedService;
  }
  
  private Service deepClone(Service lookedUpService) throws CoreException {
    AdaptrisMarshaller marshaller = DefaultMarshaller.getDefaultMarshaller();
    return (Service) marshaller.unmarshal(marshaller.marshal(lookedUpService));
  }

  @Override
  public void init() throws CoreException {
    getProxiedService().init();
  }

  @Override
  public void start() throws CoreException {
    getProxiedService().start();
  }

  @Override
  public void stop() {
    getProxiedService().stop();
  }

  @Override
  public void close() {
    getProxiedService().close();
  }

  @Override
  public void prepare() throws CoreException {
    getProxiedService().prepare();
  }

  @Override
  public String createName() {
    return getProxiedService().createName();
  }

  @Override
  public String createQualifier() {
    return getProxiedService().createQualifier();
  }

  @Override
  public boolean isTrackingEndpoint() {
    return getProxiedService().isTrackingEndpoint();
  }

  @Override
  public boolean isConfirmation() {
    return getProxiedService().isConfirmation();
  }

  @Override
  public ComponentState retrieveComponentState() {
    return getProxiedService().retrieveComponentState();
  }

  @Override
  public void changeState(ComponentState newState) {
    getProxiedService().changeState(newState);    
  }

  @Override
  public void requestInit() throws CoreException {
    getProxiedService().requestInit();
  }

  @Override
  public void requestStart() throws CoreException {
    getProxiedService().requestStart();
  }

  @Override
  public void requestStop() {
    getProxiedService().requestStop();
  }

  @Override
  public void requestClose() {
    getProxiedService().requestClose();
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    getProxiedService().doService(msg);
  }

  @Override
  public void setUniqueId(String uniqueId) {
    getProxiedService().setUniqueId(uniqueId);
  }

  @Override
  public String getUniqueId() {
    return getProxiedService().getUniqueId();
  }

  @Override
  public boolean isBranching() {
    return getProxiedService().isBranching();
  }

  @Override
  public boolean continueOnFailure() {
    return getProxiedService().continueOnFailure();
  }

  public String getLookupName() {
    return lookupName;
  }

  public void setLookupName(String lookupName) {
    this.lookupName = lookupName;
  }
  
  public boolean cloneService() {
    return this.getCloneService() != null ? true : this.getCloneService();
  }

  public Boolean getCloneService() {
    return cloneService;
  }

  public void setCloneService(Boolean cloneService) {
    this.cloneService = cloneService;
  }

}
