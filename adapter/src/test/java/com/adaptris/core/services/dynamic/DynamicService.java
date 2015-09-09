package com.adaptris.core.services.dynamic;

import java.util.HashMap;
import java.util.Map;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;

/**
 * <p>
 * Test service that records the state.
 * This is purely to test functionality for DynamicServiceLocator.
 * </p>
 */
public class DynamicService extends ServiceImp {
  public static enum State { STATELESS, INIT, STARTED, STOPPED, CLOSE }
  
  private static final Map<Class, State> SERVICE_STATE_MAP = new HashMap<Class, State>();
  
  public DynamicService() {
    super();
    SERVICE_STATE_MAP.put(this.getClass(), State.STATELESS);
  }
  
  /** @see com.adaptris.core.Service
   *   #doService(com.adaptris.core.AdaptrisMessage) */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    ;
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  public void init() throws CoreException {
    SERVICE_STATE_MAP.put(this.getClass(), State.INIT);
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  public void close() {
    SERVICE_STATE_MAP.put(this.getClass(), State.CLOSE);
  }
  
  public void start() throws CoreException {
    SERVICE_STATE_MAP.put(this.getClass(), State.STARTED);
  }
  
  
  public void stop() {
    SERVICE_STATE_MAP.put(this.getClass(), State.STOPPED);
  }
 
  public static State currentState(Class clazz) throws Exception {
    if (!SERVICE_STATE_MAP.containsKey(clazz)) {
      throw new Exception(clazz + " not registered");
    }
    return SERVICE_STATE_MAP.get(clazz);
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Standard);
  }
}
