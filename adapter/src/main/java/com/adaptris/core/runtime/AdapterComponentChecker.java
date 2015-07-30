package com.adaptris.core.runtime;

import static com.adaptris.core.runtime.AdapterComponentMBean.ID_PREFIX;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AllowsRetriesConnection;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.DefaultSerializableMessageTranslator;
import com.adaptris.core.LicensedComponent;
import com.adaptris.core.SerializableAdaptrisMessage;
import com.adaptris.core.Service;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.license.License;

/**
 * Implementation of {@link AdapterComponentCheckerMBean} for use by the GUI to check components.
 * 
 * @author lchan
 * 
 */
public class AdapterComponentChecker implements AdapterComponentCheckerMBean, ChildRuntimeInfoComponent {
  private transient AdapterManager parent;
  private transient ObjectName myObjectName = null;
  private transient DefaultSerializableMessageTranslator messageTranslator;

  private AdapterComponentChecker() {
    super();
    messageTranslator = new DefaultSerializableMessageTranslator();
  }

  public AdapterComponentChecker(AdapterManager owner) throws MalformedObjectNameException, CoreException {
    this();
    parent = owner;
    initMembers();
  }
  
  private void initMembers() throws MalformedObjectNameException {
    // Builds up a name com.adaptris:type=ComponentChecker,adapter=<adapter-id>,id=Classname
    // There can be only one LogHandler per adapter.
    myObjectName = ObjectName.getInstance(COMPONENT_CHECKER_TYPE + parent.createObjectHierarchyString() + ID_PREFIX
        + this.getClass().getSimpleName());
  }

  
  @Override
  public ObjectName getParentObjectName() throws MalformedObjectNameException {
    return parent.createObjectName();
  }

  @Override
  public String getParentId() {
    return parent.getUniqueId();
  }

  @Override
  public ObjectName createObjectName() throws MalformedObjectNameException {
    return myObjectName;
  }

  @Override
  public void registerMBean() throws CoreException {
    try {
      JmxHelper.register(createObjectName(), this);
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  @Override
  public void unregisterMBean() throws CoreException {
    try {
      JmxHelper.unregister(createObjectName());
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }


  @Override
  public RuntimeInfoComponent getParentRuntimeInfoComponent() {
    return parent;
  }

  @Override
  public void checkInitialise(String xml) throws CoreException {
    AdaptrisMarshaller marshaller = DefaultMarshaller.getDefaultMarshaller();
    AdaptrisComponent component = (AdaptrisComponent) marshaller.unmarshal(xml);
    checkCurrentLicense(component);
    if (component instanceof AllowsRetriesConnection) {
      AllowsRetriesConnection retry = (AllowsRetriesConnection) component;
      if (retry.connectionAttempts() == -1) {
        retry.setConnectionAttempts(0);
      }
    }
    try {
      LifecycleHelper.init(component);
    } finally {
      LifecycleHelper.close(component);
    }
  }

  @Override
  public SerializableAdaptrisMessage applyService(String xml, SerializableAdaptrisMessage serializedMsg) throws CoreException {
    AdaptrisMarshaller marshaller = DefaultMarshaller.getDefaultMarshaller();
    Service service = (Service) marshaller.unmarshal(xml);
    checkCurrentLicense(service);
    AdaptrisMessage msg = messageTranslator.translate(serializedMsg);
    try {
      LifecycleHelper.init(service);
      LifecycleHelper.start(service);
      service.doService(msg);
    }
    finally {
      LifecycleHelper.stop(service);
      LifecycleHelper.close(service);
    }
    return messageTranslator.translate(msg);
  }

  private void checkCurrentLicense(LicensedComponent component) throws CoreException {
    License l = parent.getWrappedComponent().currentLicense();
    if (!component.isEnabled(l)) {
      throw new CoreException("Current Adapter License is not valid for this component");
    }
  }
}
