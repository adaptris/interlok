package com.adaptris.core;

import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.LifecycleHelper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

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
@JacksonXmlRootElement(localName = "shared-service")
@XStreamAlias("shared-service")
@AdapterComponent
@ComponentProfile(summary = "A Service that refers to another Service configured elsewhere",
tag = "service")
@DisplayOrder(order = {"lookupName", "cloneService"})
public class SharedService extends SharedServiceImpl {

@AdvancedConfig
@InputFieldDefault(value="true")
private Boolean cloneService;
/**
* Set the name of the service that will be looked up from
* {@link SharedComponentList#getServices()},
*
*/
@NotBlank
@Getter
@Setter
private String lookupName;

private transient Service clonedService;

public SharedService() {
}

public SharedService(String lookupName) {
this();
setLookupName(lookupName);
}

private Service getProxiedService() {
try {
if (clonedService == null) {
Service lookedUpService = (Service) triggerJndiLookup(getLookupName());
if(cloneService())
clonedService = deepClone(lookedUpService);
else
clonedService = lookedUpService;
}
}
catch (CoreException e) {
throw new RuntimeException(e);
}
return clonedService;
}

@Override
public void init() throws CoreException {
LifecycleHelper.init(getProxiedService());
}

@Override
public void start() throws CoreException {
LifecycleHelper.start(getProxiedService());
}

@Override
public void stop() {
LifecycleHelper.stop(getProxiedService());
}

@Override
public void close() {
LifecycleHelper.close(getProxiedService());
}

@Override
public void prepare() throws CoreException {
LifecycleHelper.registerEventHandler(getProxiedService(), eventHandler);
LifecycleHelper.prepare(getProxiedService());
}

@Override
public void doService(AdaptrisMessage msg) throws ServiceException {
applyService(getProxiedService(), msg);
}

@Override
public boolean isBranching() {
return getProxiedService().isBranching();
}

boolean cloneService() {
return BooleanUtils.toBooleanDefaultIfNull(getCloneService(), true);
}

public Boolean getCloneService() {
return cloneService;
}

public void setCloneService(Boolean cloneService) {
this.cloneService = cloneService;
}

Service getClonedService() {
return clonedService;
}

void setClonedService(Service clonedService) {
this.clonedService = clonedService;
}

}
