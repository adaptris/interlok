/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.services.dynamic;

import static com.adaptris.core.util.LoggingHelper.friendlyName;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.TradingRelationship;
import com.adaptris.core.TradingRelationshipCreator;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.LoggingHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of {@link com.adaptris.core.Service} which dynamically obtains and applies a
 * {@link com.adaptris.core.Service} to an {@link com.adaptris.core.AdaptrisMessage} based on its
 * {@link TradingRelationship}.
 * </p>
 * 
 * @config dynamic-service-locator
 * @deprecated since 3.8.4 use {@link DynamicServiceExecutor} with a URL based
 *             {@link ServiceExtractor} instead.
 * 
 */
@Deprecated
@XStreamAlias("dynamic-service-locator")
@AdapterComponent
@ComponentProfile(summary = "Locate and execute a service definition based on attributes of the message", tag = "service,dynamic")
@DisplayOrder(order = {"treatNotFoundAsError"})
@Removal(version = "3.11.0")
public class DynamicServiceLocator extends ServiceImp implements EventHandlerAware {

  @NotNull
  @Valid
  private TradingRelationshipCreator tradingRelationshipCreator;
  @NotNull
  @AutoPopulated
  @Valid
  @AdvancedConfig
  private MatchingStrategy matchingStrategy;
  @NotNull
  @Valid
  private ServiceNameProvider serviceNameProvider;
  @NotNull
  @Valid
  private ServiceStore serviceStore;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean treatNotFoundAsError;

  private transient EventHandler eventHandler;

  private static transient boolean warningLogged = false;
  /**
   * Creates a new Instance.
   * <p>
   * The following are the defaults
   * <ul>
   * <li>matchingStrategy is {@link ExactMatchingStrategy}.</li>
   * <li>treatNotFoundAsError is false</li>
   * </p>
   */
  public DynamicServiceLocator() {
    LoggingHelper.logDeprecation(warningLogged, () -> {
      warningLogged = true;
    }, this.getClass().getSimpleName(), DynamicServiceExecutor.class.getName());
    // defaults...
    setMatchingStrategy(new ExactMatchingStrategy());
  }

  /**
   * Performs the service.
   * <p>
   * Creates the {@link TradingRelationship} (which may not contain wildcards),
   * applies the configured {@link MatchingStrategy} to obtain a list of other
   * {@link TradingRelationship} to look for in the event of no exact match,
   * obtains the logical name to look for from the {@link ServiceNameProvider},
   * retrieves the {@link com.adaptris.core.Service} stored against this name in the
   * {@link ServiceStore}, and then applies the service against the message.
   * </p>
   *
   * @see com.adaptris.core.Service#doService
   *      (com.adaptris.core.AdaptrisMessage)
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      TradingRelationship t = tradingRelationshipCreator.create(msg);

      if (t.hasWildCards()) {
        throw new ServiceException(t + " contains wild cards and is invalid");
      }

      TradingRelationship[] matches = matchingStrategy.create(t);

      String name = serviceNameProvider.obtain(matches);
      Service dynamicService = serviceStore.obtain(name);

      if (dynamicService != null) {
        execute(name, dynamicService, msg);
      }
      else {
        if (treatNotFoundAsError()) {
          throw new ServiceException(name + " was not found");
        }
        else {
          log.debug(name + " was not found, no services applied");
        }
      }
    }
    catch (ServiceException e) {
      throw e;
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
  }

  private void execute(String matchedName, Service dynamicService, AdaptrisMessage msg) throws CoreException {
    String serviceName = friendlyName(dynamicService);

    LifecycleHelper.prepare(dynamicService);
    log.debug("Applying service [" + serviceName + "] from [" + matchedName + "]");
    LifecycleHelper.registerEventHandler(dynamicService, eventHandler);
    try {
      start(dynamicService);
      dynamicService.doService(msg);
    }
    finally {
      log.trace("Stopping services");
      stop(dynamicService);
    }
  }

  private static void start(AdaptrisComponent c) throws CoreException {
    LifecycleHelper.init(c);
    try {
      LifecycleHelper.start(c);
    }
    catch (CoreException e) {
      LifecycleHelper.close(c);
      throw e;
    }
  }

  private static void stop(AdaptrisComponent c) {
    LifecycleHelper.stop(c);
    LifecycleHelper.close(c);
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected void initService() throws CoreException {
    try {
      Args.notNull(getTradingRelationshipCreator(), "relationshipCreator");
      Args.notNull(getServiceNameProvider(), "serviceNameProvider");
      Args.notNull(getServiceStore(), "serviceStore");
      getServiceStore().validate();
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void closeService() {
  }

  // getters & setters...

  /**
   * <p>
   * Returns the <code>ServiceStore</code> to use.
   * </p>
   *
   * @return the <code>ServiceStore</code> to use
   */
  public ServiceStore getServiceStore() {
    return serviceStore;
  }

  /**
   * <p>
   * Sets the <code>ServiceStore</code> to use. May not be null.
   * </p>
   *
   * @param s the <code>ServiceStore</code> to use
   */
  public void setServiceStore(ServiceStore s) {
    serviceStore = Args.notNull(s, "serviceStore");

  }

  /**
   * <p>
   * Returns the <code>TradingRelationshipCreator</code> to use.
   * </p>
   *
   * @return the <code>TradingRelationshipCreator</code> to use
   */
  public TradingRelationshipCreator getTradingRelationshipCreator() {
    return tradingRelationshipCreator;
  }

  /**
   * <p>
   * Sets the <code>TradingRelationshipCreator</code> to use. May not be null.
   * </p>
   *
   * @param t the <code>TradingRelationshipCreator</code> to use
   */
  public void setTradingRelationshipCreator(TradingRelationshipCreator t) {
    tradingRelationshipCreator = Args.notNull(t, "tradingRelationshipCreator");
  }

  /**
   * <p>
   * Returns the <code>ServiceNameProvider</code> to use.
   * </p>
   *
   * @return the <code>ServiceNameProvider</code> to use
   */
  public ServiceNameProvider getServiceNameProvider() {
    return serviceNameProvider;
  }

  /**
   * <p>
   * Sets the <code>ServiceNameProvider</code> to use. May not be null.
   * </p>
   *
   * @param s the <code>ServiceNameProvider</code> to use
   */
  public void setServiceNameProvider(ServiceNameProvider s) {
    serviceNameProvider = Args.notNull(s, "serviceNameProvider");
  }

  /**
   * <p>
   * Returns the <code>MatchingStrategy</code> to use.
   * </p>
   *
   * @return the <code>MatchingStrategy</code> to use
   */
  public MatchingStrategy getMatchingStrategy() {
    return matchingStrategy;
  }

  /**
   * <p>
   * Sets the <code>MatchingStrategy</code> to use. May not be null.
   * </p>
   *
   * @param m the <code>MatchingStrategy</code> to use
   */
  public void setMatchingStrategy(MatchingStrategy m) {
    matchingStrategy = Args.notNull(m, "matchingStrategy");
  }

  /**
   * @return the treatNotFoundAsError
   */
  public Boolean getTreatNotFoundAsError() {
    return treatNotFoundAsError;
  }

  /**
   * Specify whether a failure to find a dynamic service is treated as an
   * exception.
   *
   * @param b if true then a ServiceException is thrown if the
   *        <code>ServiceStore.obtain(String)</code> returns null
   * @see ServiceStore#obtain(String)
   */
  public void setTreatNotFoundAsError(Boolean b) {
    treatNotFoundAsError = b;
  }

  boolean treatNotFoundAsError() {
    return BooleanUtils.toBooleanDefaultIfNull(getTreatNotFoundAsError(), false);
  }
  /**
   * @see EventHandlerAware#registerEventHandler(com.adaptris.core.EventHandler)
   */
  public void registerEventHandler(EventHandler eh) {
    eventHandler = eh;
  }
}
