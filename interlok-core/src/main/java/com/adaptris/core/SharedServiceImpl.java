package com.adaptris.core;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.annotation.MarshallingCDATA;

public abstract class SharedServiceImpl extends SharedComponent implements Service, EventHandlerAware {


  @NotBlank
  @AutoPopulated
  private String uniqueId;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean continueOnFail;
  @InputFieldDefault(value = "true")
  private Boolean enabled;
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "false")
  private Boolean isTrackingEndpoint;
  @MarshallingCDATA
  private String comments;

  protected transient EventHandler eventHandler;
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  private transient ComponentState serviceState;

  public SharedServiceImpl() {
    setUniqueId(new GuidGenerator().getUUID());
    changeState(ClosedState.getInstance());
  }

  protected void applyService(Service s, AdaptrisMessage msg) throws ServiceException {
    try {
      s.doService(msg);
      msg.addEvent(s, true);
    } catch (Exception e) {
      msg.addEvent(s, false);
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void setComments(String s) {
    comments = s;
  }

  @Override
  public String getComments() {
    return comments;
  }
  
  protected Service deepClone(Service lookedUpService) throws CoreException {
    AdaptrisMarshaller marshaller = DefaultMarshaller.getDefaultMarshaller();
    return (Service) marshaller.unmarshal(marshaller.marshal(lookedUpService));
  }

  @Override
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

  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  @Override
  public void setUniqueId(String uniqueId) {
    this.uniqueId = Args.notBlank(uniqueId, "uniqueId");
  }

  @Override
  public boolean isTrackingEndpoint() {
    return BooleanUtils.toBooleanDefaultIfNull(getIsTrackingEndpoint(), false);
  }

  @Override
  public boolean continueOnFailure() {
    return BooleanUtils.toBooleanDefaultIfNull(getContinueOnFail(), false);
  }

  public Boolean getContinueOnFail() {
    return continueOnFail;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }
  
  public boolean enabled() {
    return BooleanUtils.toBooleanDefaultIfNull(getEnabled(), true);
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
