package com.adaptris.core;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;
import com.adaptris.util.GuidGenerator;

public abstract class SharedServiceImpl extends SharedComponent implements Service, EventHandlerAware {

  @NotBlank
  private String lookupName;
  @NotBlank
  @AutoPopulated
  private String uniqueId;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean continueOnFail;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean isTrackingEndpoint;

  protected transient EventHandler eventHandler;
  private transient ComponentState serviceState;

  public SharedServiceImpl() {
    setUniqueId(new GuidGenerator().getUUID());
    changeState(ClosedState.getInstance());
  }
  
  protected Service deepClone(Service lookedUpService) throws CoreException {
    AdaptrisMarshaller marshaller = DefaultMarshaller.getDefaultMarshaller();
    return (Service) marshaller.unmarshal(marshaller.marshal(lookedUpService));
  }

  public String getLookupName() {
    return lookupName;
  }

  public void setLookupName(String lookupName) {
    this.lookupName = lookupName;
  }
  
  public void registerEventHandler(EventHandler eh) {
    eventHandler = eh;
  }

  @Override
  public void changeState(ComponentState newState) {
    serviceState = newState;
  }

  @Override
  public ComponentState retrieveComponentState() {
    return serviceState;
  }

  @Override
  public void requestInit() throws CoreException {
    serviceState.requestInit(this);
  }

  @Override
  public void requestStart() throws CoreException {
    serviceState.requestStart(this);
  }

  @Override
  public void requestStop() {
    serviceState.requestStop(this);
  }

  @Override
  public void requestClose() {
    serviceState.requestClose(this);
  }

  public String getUniqueId() {
    return uniqueId;
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = Args.notBlank(uniqueId, "uniqueId");
  }

  @Override
  public boolean isTrackingEndpoint() {
    return getIsTrackingEndpoint() != null ? getIsTrackingEndpoint().booleanValue() : false;
  }

  @Override
  public boolean isConfirmation() {
    return false;
  }

  @Override
  public boolean continueOnFailure() {
    return getContinueOnFail() != null ? getContinueOnFail().booleanValue() : false;
  }

  public Boolean getContinueOnFail() {
    return continueOnFail;
  }

  public void setContinueOnFail(Boolean b) {
    continueOnFail = b;
  }

  public Boolean getIsTrackingEndpoint() {
    return isTrackingEndpoint;
  }

  public void setIsTrackingEndpoint(Boolean b) {
    isTrackingEndpoint = b;
  }

  @Override
  public String createName() {
    return this.getClass().getName();
  }

  @Override
  public String createQualifier() {
    return defaultIfEmpty(getUniqueId(), "");
  }

}
