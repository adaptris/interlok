/*
 * $RCSfile: ServiceList.java,v $
 * $Revision: 1.26 $
 * $Date: 2005/09/23 00:56:54 $
 * $Author: hfraser $
 */
package com.adaptris.core;

import static com.adaptris.core.util.LoggingHelper.friendlyName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>ServiceCollection</code> in which an ordered list of <code>Service</code>s are applied to a message.
 * </p>
 * 
 * @config service-list
 */
@XStreamAlias("service-list")
public class ServiceList extends ServiceCollectionImp {

  public ServiceList() {
    super();
  }

  public ServiceList(Collection<Service> serviceList) {
    super(serviceList);
  }

  public ServiceList(Service[] serviceList) {
    super(new ArrayList<Service>(Arrays.asList(serviceList)));
  }

  @Override
  protected void applyServices(AdaptrisMessage msg) throws ServiceException {
    for (Service service : this) {
      try {
        String serviceName = friendlyName(service);
        
        /* 
         * Check this before applying any services as it may have been set deep in the ServiceList hierarchy. If we don't
         * check this before applying a service, we'll be "randomly" applying one service at each service list, keeping the
         * metadata and then no longer apply the next ones. That's strange behaviour and contradicts what the javadoc
         * says about CoreConstants.STOP_PROCESSING_KEY
         */
        if (CoreConstants.STOP_PROCESSING_VALUE.equals(msg.getMetadataValue(CoreConstants.STOP_PROCESSING_KEY))) {
          log.trace("Service " + serviceName + " has added metadata which stops any further configured"
                  + " services being applied");
          break; // apply no more Services...
        }
        
        service.doService(msg);
        // add success event
        msg.addEvent(service, true);
        log.debug("service [" + serviceName + "] applied");
      }
      catch (Exception e) {
        // add fail event
        msg.addEvent(service, false);
        handleException(service, msg, e);
      }
    }
  }

  @Override
  protected void doClose() {
  }

  @Override
  protected void doInit() throws CoreException {
  }

  @Override
  protected void doStart() throws CoreException {
  }

  @Override
  protected void doStop() {
  }
}
