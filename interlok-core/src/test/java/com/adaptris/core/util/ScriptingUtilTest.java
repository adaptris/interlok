package com.adaptris.core.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import org.junit.Test;

public class ScriptingUtilTest {

  @Test
  public void testLanguageJavascript_NashornEngine() throws Exception {
    ScriptEngineManager engineManager = initEngineManager();
    assertTrue(ScriptingUtil.dependsOnNashorn(engineManager, "javascript"));
  }

  @Test
  public void testLanguageJavascript_GraalEngineAvailable() throws Exception {
    ScriptEngineManager engineManager = initEngineManager();
    ScriptEngine scriptEngine = mock(ScriptEngine.class);
    when(engineManager.getEngineByName(ScriptingUtil.GRAAL_JS_ENGINE)).thenReturn(scriptEngine);
    assertFalse(ScriptingUtil.dependsOnNashorn(engineManager, "javascript"));
  }

  @Test
  public void testLanguageNashorn_GraalEngineAvailable() throws Exception {
    ScriptEngineManager engineManager = initEngineManager();
    ScriptEngine scriptEngine = mock(ScriptEngine.class);
    when(engineManager.getEngineByName(ScriptingUtil.GRAAL_JS_ENGINE)).thenReturn(scriptEngine);
    assertTrue(ScriptingUtil.dependsOnNashorn(engineManager, ScriptingUtil.NASHORN));
  }

  @Test
  public void testLanguageNashorn_NashornEngine() throws Exception {
    ScriptEngineManager engineManager = initEngineManager();
    assertTrue(ScriptingUtil.dependsOnNashorn(engineManager, ScriptingUtil.NASHORN));
  }

  @Test
  public void testLanguageJruby() throws Exception {
    ScriptEngineManager engineManager = initEngineManager();
    assertFalse(ScriptingUtil.dependsOnNashorn(engineManager, "jruby"));
  }


  private ScriptEngineManager initEngineManager() {
    ScriptEngineManager engineManager = mock(ScriptEngineManager.class);

    ScriptEngineFactory graaljsEngineFactory = mock(ScriptEngineFactory.class);
    when(graaljsEngineFactory.getEngineName()).thenReturn(ScriptingUtil.GRAAL_JS_ENGINE);
    ScriptEngineFactory nashornEngineFactory = mock(ScriptEngineFactory.class);
    when(nashornEngineFactory.getEngineName()).thenReturn(ScriptingUtil.NASHORN_ENGINE);
    when(nashornEngineFactory.getNames()).thenReturn(List.of("nashorn", "Nashorn", "js", "JS",
        "JavaScript", "javascript", "ECMAScript", "ecmascript"));
    when(engineManager.getEngineFactories())
        .thenReturn(List.of(graaljsEngineFactory, nashornEngineFactory));
    return engineManager;
  }
}
