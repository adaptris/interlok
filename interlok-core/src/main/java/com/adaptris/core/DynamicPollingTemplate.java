/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core;

import javax.validation.Valid;
import com.adaptris.core.PollingTrigger.MessageProvider;
import com.adaptris.core.http.client.net.HttpRequestService;
import com.adaptris.core.services.ScriptingServiceImp;
import com.adaptris.core.services.jdbc.JdbcDataQueryService;
import com.adaptris.core.services.jdbc.ResultSetTranslator;
import com.adaptris.core.services.jdbc.SplittingXmlPayloadTranslator;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Provides a template for {@link PollingTrigger}.
 * <p>
 * This {@link MessageProvider} implementation allows you to use an implementation of
 * {@link DynamicPollingTemplate.TemplateProvider} to dynamically populate the contents of the
 * message before submitting to a workflow. At the moment only a limited subset of services are
 * enabled for this provider :
 * <ul>
 * <li>{@link ScriptingServiceImp}</li>
 * <li>{@link JdbcDataQueryService}</li>
 * <li>{@link HttpRequestService}</li>
 * </p>
 * <p>
 * If you opt to use {@link JdbcDataQueryService} or {@link HttpRequestService} then there will be
 * some types of configuration that will make no sense in this context such as
 * <strong>metadata/xpath driven parameters</strong> or use of some {@link ResultSetTranslator}
 * implementations (most notably {@link SplittingXmlPayloadTranslator}
 * </p>
 *
 * @config dynamic-polling-trigger-template
 */
@XStreamAlias("dynamic-polling-trigger-template")
public class DynamicPollingTemplate implements PollingTrigger.MessageProvider {

  private static final TemplateProvider DEFAULT_IMP = new NullMessageProvider();

  @Valid
  private TemplateProvider template;

  public DynamicPollingTemplate() {

  }

  public DynamicPollingTemplate(TemplateProvider s) {
    this();
    setTemplate(s);
  }

  @Override
  public void init() throws CoreException {
    LifecycleHelper.prepare(getTemplate());
    LifecycleHelper.init(getTemplate());
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(getTemplate());
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(getTemplate());
  }

  @Override
  public void close() {
    LifecycleHelper.close(getTemplate());
  }


  public TemplateProvider getTemplate() {
    return template;
  }

  public void setTemplate(TemplateProvider provider) {
    this.template = provider;
  }

  TemplateProvider messageProvider() {
    return getTemplate() != null ? getTemplate() : DEFAULT_IMP;
  }

  @Override
  public AdaptrisMessage createMessage(AdaptrisMessageFactory fac) throws CoreException {
    AdaptrisMessage msg = fac.newMessage();
    messageProvider().doService(msg);
    return msg;
  }

  /**
   * Marker interface so that not all services can be configured.
   *
   *
   */
  public interface TemplateProvider extends Service {

  }

  private static class NullMessageProvider extends NullService implements TemplateProvider {

  }


}
