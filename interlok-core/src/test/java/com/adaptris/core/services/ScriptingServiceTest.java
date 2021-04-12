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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.GeneralServiceExample;
import com.adaptris.core.util.LifecycleHelper;

public class ScriptingServiceTest extends GeneralServiceExample {

  private static final String KEY_SCRIPTING_BASEDIR = "scripting.basedir";
  public static final String SCRIPT = "\nvalue = message.getMetadataValue 'MyMetadataKey';"
      + "\nmessage.addMetadata('MyMetadataKey', value.reverse);";

  private static final String MY_METADATA_VALUE = "MyMetadataValue";
  private static final String MY_METADATA_KEY = "MyMetadataKey";



  private File writeScript(boolean working) throws IOException {
    File result = null;
    File dir = new File(PROPERTIES.getProperty(KEY_SCRIPTING_BASEDIR, "./build/scripting"));
    dir.mkdirs();
    result = File.createTempFile("junit", ".script", dir);
    try (FileWriter fw = new FileWriter(result)) {
      fw.write(working ? SCRIPT : "This will fail");
    }
    return result;
  }

  private void delete(File script) throws Exception {
    if (script == null) {
      return;
    }
    if (!script.delete()) {
      throw new Exception("failed to delete file");
    }
  }

  @Test
  public void testService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(MY_METADATA_KEY, MY_METADATA_VALUE);
    ScriptingService service = createService();
    File script = writeScript(true);
    service.setScriptFilename(script.getCanonicalPath());
    execute(service, msg);
    assertTrue(msg.headersContainsKey(MY_METADATA_KEY));
    assertNotSame(MY_METADATA_VALUE, msg.getMetadataValue(MY_METADATA_KEY));
    assertEquals(new StringBuffer(MY_METADATA_VALUE).reverse().toString(), msg.getMetadataValue(MY_METADATA_KEY));
    delete(script);
  }

  @Test
  public void testInit() throws Exception {
    ScriptingService service = new ScriptingService();

    assertThrows("Service initialised w/o a language", Exception.class, () -> {
      service.init();
    });
    service.setLanguage("jruby");
    service.setScriptFilename("/BLAHBLAHBLAHBLAHBLAH/BLAHBLAHBLAHBLAH");
    assertThrows("Service initialised with no idiotic filename", Exception.class, () -> {
      service.init();
    });
    File script = writeScript(false);
    service.setScriptFilename(script.getCanonicalPath());
    LifecycleHelper.init(service);
    LifecycleHelper.close(service);
    delete(script);
  }

  @Test
  public void testInitWithJsLanguage() throws Exception {
    ScriptingService service = new ScriptingService();
    File script = writeScript(false);
    service.setScriptFilename(script.getCanonicalPath());
    service.setLanguage("nashorn");

    LifecycleHelper.init(service);
    LifecycleHelper.close(service);

    service.setLanguage("javascript");

    LifecycleHelper.init(service);
    LifecycleHelper.close(service);

    service.setLanguage("js");

    LifecycleHelper.init(service);
    LifecycleHelper.close(service);

    service.setLanguage("ecmascript");

    LifecycleHelper.init(service);
    LifecycleHelper.close(service);

    delete(script);
  }

  @Test
  public void testLanguageJavascriptEngineNashorn() throws Exception {
    ScriptingService service = new ScriptingService();
    service.setLanguage("javascript");

    ScriptEngineManager engineManager = initEngineManager();

    ScriptEngine scriptEngine = mock(ScriptEngine.class);
    when(engineManager.getEngineByName(ScriptingService.NASHORN_ENGINE)).thenReturn(scriptEngine);

    service.checkEngine(engineManager);
  }

  @Test
  public void testLanguageJavascriptEngineGraalJs() throws Exception {
    ScriptingService service = new ScriptingService();
    service.setLanguage("javascript");

    ScriptEngineManager engineManager = initEngineManager();

    ScriptEngine scriptEngine = mock(ScriptEngine.class);
    when(engineManager.getEngineByName(ScriptingService.GRAAL_JS_ENGINE)).thenReturn(scriptEngine);

    service.checkEngine(engineManager);
  }

  @Test
  public void testLanguageJavascriptEngineNoNashorn() throws Exception {
    ScriptingService service = new ScriptingService();
    service.setLanguage("javascript");

    ScriptEngineManager engineManager = initEngineManager();

    when(engineManager.getEngineByName(ScriptingService.NASHORN_ENGINE)).thenReturn(null);

    service.checkEngine(engineManager);
  }

  @Test
  public void testLanguageNashornEngineNashorn() throws Exception {
    ScriptingService service = new ScriptingService();
    service.setLanguage(ScriptingService.NASHORN);

    ScriptEngine scriptEngine = mock(ScriptEngine.class);
    ScriptEngineManager engineManager = mock(ScriptEngineManager.class);
    when(engineManager.getEngineByName(ScriptingService.NASHORN_ENGINE)).thenReturn(scriptEngine);

    service.checkEngine(engineManager);
  }

  @Test
  public void testLanguageNashornEngineNashornAndGraalJs() throws Exception {
    ScriptingService service = new ScriptingService();
    service.setLanguage(ScriptingService.NASHORN);

    ScriptEngine scriptEngine = mock(ScriptEngine.class);
    ScriptEngineManager engineManager = mock(ScriptEngineManager.class);
    when(engineManager.getEngineByName(ScriptingService.NASHORN_ENGINE)).thenReturn(scriptEngine);
    when(engineManager.getEngineByName(ScriptingService.GRAAL_JS_ENGINE)).thenReturn(scriptEngine);

    service.checkEngine(engineManager);
  }

  @Test
  public void testLanguageJruby() throws Exception {
    ScriptingService service = new ScriptingService();
    service.setLanguage("jruby");

    ScriptEngine scriptEngine = mock(ScriptEngine.class);
    ScriptEngineManager engineManager = mock(ScriptEngineManager.class);
    when(engineManager.getEngineByName("jruby")).thenReturn(scriptEngine);

    service.checkEngine(engineManager);
  }

  @Test
  public void testDoServiceWithFailingScript() throws Exception {
    ScriptingService service = createService();
    File script = writeScript(false);
    service.setScriptFilename(script.getCanonicalPath());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertThrows("Service failure expected", Exception.class, () -> {
      execute(service, msg);
    });
    delete(script);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    ScriptingService service = createService();
    service.setScriptFilename("/path/to/script/you/want/to/execute");
    return service;
  }

  private ScriptingService createService() {
    ScriptingService result = new ScriptingService("scripting-service");
    result.setLanguage("jruby");
    return result;
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!--"
        + "\nThis allows to embed scripts written in any language that supports JSR223 (e.g. jruby)."
        + "\nThe script is executed and the AdaptrisMessage that is due to be processed is"
        + "\nbound against the key 'message' and an instance of org.slf4j.Logger is also bound "
        + "\nto key 'log'. These can be used as a standard variable within the script." + "\n-->\n";
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
