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

package com.adaptris.core.services;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.GeneralServiceExample;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.util.LifecycleHelper;

public class EmbeddedScriptingServiceTest extends GeneralServiceExample {

  private static final String MY_METADATA_KEY2 = "MyMetadataKey2";
  private static final String MY_METADATA_KEY3 = "MyMetadataKey3";
  private static final String SERVICE_UID = "embedded-script";
  private static final String NEXT_SERVICE_ID = "NextService";
  private static final String MY_METADATA_VALUE = "MyMetadataValue";
  private static final String MY_METADATA_KEY = "MyMetadataKey";


  @Test
  public void testService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(MY_METADATA_KEY, MY_METADATA_VALUE);
    EmbeddedScriptingService service = createService(getName());
    assertFalse(service.isBranching());
    execute(service, msg);
    assertTrue(msg.headersContainsKey(MY_METADATA_KEY));
    assertNotSame(MY_METADATA_VALUE, msg.getMetadataValue(MY_METADATA_KEY));
    assertEquals(new StringBuffer(MY_METADATA_VALUE).reverse().toString(), msg.getMetadataValue(MY_METADATA_KEY));
  }

  @Test
  public void testBranchingService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(MY_METADATA_KEY, MY_METADATA_VALUE);
    EmbeddedScriptingService service = createServiceForBranch(getName(), NEXT_SERVICE_ID);
    assertTrue(service.isBranching());
    execute(service, msg);
    assertEquals(NEXT_SERVICE_ID, msg.getNextServiceId());
  }

  @Test
  public void testBranchingServiceExcecution_NextServiceId() throws Exception {
    BranchingServiceCollection bsc = new BranchingServiceCollection();
    bsc.setFirstServiceId(getName());
    bsc.add(createServiceForBranch(getName(), NEXT_SERVICE_ID));
    AddMetadataService next = new AddMetadataService(new ArrayList<>(Arrays.asList(new MetadataElement(
        MY_METADATA_KEY2, MY_METADATA_VALUE))));
    next.setUniqueId(NEXT_SERVICE_ID);
    bsc.add(next);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(MY_METADATA_KEY, MY_METADATA_VALUE);
    execute(bsc, msg);
    assertEquals(MY_METADATA_VALUE, msg.getMetadataValue(MY_METADATA_KEY));
    assertEquals(MY_METADATA_VALUE, msg.getMetadataValue(MY_METADATA_KEY2));
  }

  @Test
  public void testBranchingServiceExecution_NoNextServiceId() throws Exception {
    BranchingServiceCollection bsc = new BranchingServiceCollection();
    bsc.setFirstServiceId(getName());
    bsc.add(createServiceForBranch(getName(), null));
    bsc.add(createService(NEXT_SERVICE_ID));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(MY_METADATA_KEY, MY_METADATA_VALUE);
    execute(bsc, msg);
    assertTrue(msg.headersContainsKey(MY_METADATA_KEY));
    assertEquals(MY_METADATA_VALUE, msg.getMetadataValue(MY_METADATA_KEY));
    assertEquals(MY_METADATA_VALUE, msg.getMetadataValue(MY_METADATA_KEY3));
  }

  @Test
  public void testInit() throws Exception {
    EmbeddedScriptingService service = new EmbeddedScriptingService();
    assertThrows("Service initialised w/o a language", Exception.class, () -> {
      service.init();
    });
    service.setLanguage("BLAHBLAHBLAH");
    assertThrows("Service initialised BLAHBLAHBLAH", Exception.class, () -> {
      LifecycleHelper.init(service);
    });
    service.setLanguage("jruby");
    LifecycleHelper.init(service);
  }

  @Test
  public void testInitWithJsLanguage() throws Exception {
    EmbeddedScriptingService service = new EmbeddedScriptingService();
    service.setScript("// Some script");
    service.setLanguage("javascript");

    LifecycleHelper.init(service);
    LifecycleHelper.close(service);

    service.setLanguage("js");

    LifecycleHelper.init(service);
    LifecycleHelper.close(service);

    service.setLanguage("ecmascript");

    LifecycleHelper.init(service);
    LifecycleHelper.close(service);
  }

  @Test
  public void testLanguageJavascriptEngineNashorn() throws Exception {
    EmbeddedScriptingService service = new EmbeddedScriptingService();
    service.setLanguage("javascript");

    ScriptEngineManager engineManager = initEngineManager();

    ScriptEngine scriptEngine = mock(ScriptEngine.class);
    when(engineManager.getEngineByName(ScriptingService.NASHORN_ENGINE)).thenReturn(scriptEngine);

    service.checkEngine(engineManager);
  }

  @Test
  public void testLanguageJavascriptEngineGraalJs() throws Exception {
    EmbeddedScriptingService service = new EmbeddedScriptingService();
    service.setLanguage("javascript");

    ScriptEngineManager engineManager = initEngineManager();

    ScriptEngine scriptEngine = mock(ScriptEngine.class);
    when(engineManager.getEngineByName(ScriptingService.GRAAL_JS_ENGINE)).thenReturn(scriptEngine);

    service.checkEngine(engineManager);
  }

  @Test
  public void testLanguageJavascriptEngineNoNashorn() throws Exception {
    EmbeddedScriptingService service = new EmbeddedScriptingService();
    service.setLanguage("javascript");

    ScriptEngineManager engineManager = initEngineManager();

    when(engineManager.getEngineByName(ScriptingService.NASHORN_ENGINE)).thenReturn(null);

    service.checkEngine(engineManager);
  }

  @Test
  public void testLanguageNashornEngineNashorn() throws Exception {
    EmbeddedScriptingService service = new EmbeddedScriptingService();
    service.setLanguage(ScriptingService.NASHORN);

    ScriptEngine scriptEngine = mock(ScriptEngine.class);
    ScriptEngineManager engineManager = mock(ScriptEngineManager.class);
    when(engineManager.getEngineByName(ScriptingService.NASHORN_ENGINE)).thenReturn(scriptEngine);

    service.checkEngine(engineManager);
  }

  @Test
  public void testLanguageNashornEngineNashornAndGraalJs() throws Exception {
    EmbeddedScriptingService service = new EmbeddedScriptingService();
    service.setLanguage(ScriptingService.NASHORN);

    ScriptEngine scriptEngine = mock(ScriptEngine.class);
    ScriptEngineManager engineManager = mock(ScriptEngineManager.class);
    when(engineManager.getEngineByName(ScriptingService.NASHORN_ENGINE)).thenReturn(scriptEngine);
    when(engineManager.getEngineByName(ScriptingService.GRAAL_JS_ENGINE)).thenReturn(scriptEngine);

    service.checkEngine(engineManager);
  }

  @Test
  public void testLanguageJruby() throws Exception {
    EmbeddedScriptingService service = new EmbeddedScriptingService();
    service.setLanguage("jruby");

    ScriptEngine scriptEngine = mock(ScriptEngine.class);
    ScriptEngineManager engineManager = mock(ScriptEngineManager.class);
    when(engineManager.getEngineByName("jruby")).thenReturn(scriptEngine);

    service.checkEngine(engineManager);
  }

  @Test
  public void testDoServiceWithEmptyScript() throws Exception {
    EmbeddedScriptingService service = new EmbeddedScriptingService();
    service.setLanguage("jruby");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    service.setScript("");
    execute(service, msg);
  }

  @Test
  public void testDoServiceWithFailingScript() throws Exception {
    EmbeddedScriptingService service = createService(getName());
    service.setScript("This Really Should Fail");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertThrows("Service failure expected", Exception.class, () -> {
      execute(service, msg);
    });
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return createService(null);
  }

  private EmbeddedScriptingService createService(String uid) {
    EmbeddedScriptingService result = uid == null ? new EmbeddedScriptingService(SERVICE_UID) : new EmbeddedScriptingService(uid);
    result.setLanguage("jruby");
    result.setScript(ScriptingServiceTest.SCRIPT);
    return result;
  }

  private EmbeddedScriptingService createServiceForBranch(String uid, String nextServiceId) {
    EmbeddedScriptingService result = uid == null ? new EmbeddedScriptingService(SERVICE_UID) : new EmbeddedScriptingService(uid);
    result.setLanguage("nashorn");
    result.setBranchingEnabled(true);
    if (!isEmpty(nextServiceId)) {
      result.setScript("message.setNextServiceId('" + nextServiceId + "');");
    } else {
      result.setScript("message.addMetadata('" + MY_METADATA_KEY3 + "', '" + MY_METADATA_VALUE + "');");
    }
    return result;
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + "<!--"
        + "\nThis allows to embed scripts written in any language that supports JSR223 (e.g. jruby)."
        + "\nThe script is executed and the AdaptrisMessage that is due to be processed is"
        + "\nbound against the key 'message' and an instance of org.slf4j.Logger is also bound "
        + "\nto key 'log'. These can be used as a standard variable within the script."
        + "\nThe example below simply reverses an item of metadata using jruby as the scripting"
        + "\nlanguage. This isn't something that is easily supported with existing services "
        +"\n(but why would you want to do it?)" + "\n-->\n";
  }

  private ScriptEngineManager initEngineManager() {
    ScriptEngineManager engineManager = mock(ScriptEngineManager.class);

    ScriptEngineFactory graaljsEngineFactory = mock(ScriptEngineFactory.class);
    when(graaljsEngineFactory.getEngineName()).thenReturn(ScriptingService.GRAAL_JS_ENGINE);
    ScriptEngineFactory nashornEngineFactory = mock(ScriptEngineFactory.class);
    when(nashornEngineFactory.getEngineName()).thenReturn(ScriptingService.NASHORN_ENGINE);
    when(nashornEngineFactory.getNames())
    .thenReturn(List.of("nashorn", "Nashorn", "js", "JS", "JavaScript", "javascript", "ECMAScript", "ecmascript"));
    when(engineManager.getEngineFactories()).thenReturn(List.of(graaljsEngineFactory, nashornEngineFactory));
    return engineManager;
  }

}
