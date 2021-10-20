package com.adaptris.core.management.config;

import java.util.Collection;
import java.util.function.Function;
import javax.script.ScriptEngineManager;
import com.adaptris.core.Adapter;
import com.adaptris.core.services.ScriptingServiceImp;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.core.util.ObjectScanner;
import com.adaptris.core.util.ScriptingUtil;
import lombok.NoArgsConstructor;

/**
 * Configuration check that checks if a scripting service impl requires nashorn and warns about it.
 *
 */
@NoArgsConstructor
public class NashornChecker extends ValidationCheckerImpl {

  private static final String FRIENDLY_NAME = "Nashorn Scripting Engine check";


  @Override
  protected void validate(Adapter adapter, ConfigurationCheckReport report) {
    try {
      ScriptEngineManager engineManager = new ScriptEngineManager(this.getClass().getClassLoader());
      Collection<ScriptingServiceImp> scripts = scanner().scan(adapter);
      for (ScriptingServiceImp s : scripts) {
        if (ScriptingUtil.dependsOnNashorn(engineManager, s.getLanguage())) {
          report.getWarnings()
              .add(String.format(
                  "[%s] relies on the deprecated Nashorn scripting engine; consider switching to Graal JS",
              LoggingHelper.friendlyName(s)));
        }
      }
    } catch (Exception ex) {
      report.getFailureExceptions().add(ex);
    }
  }

  @Override
  public String getFriendlyName() {
    return FRIENDLY_NAME;
  }

  protected ObjectScanner<ScriptingServiceImp> scanner() {
    return new ScriptingServiceScanner();
  }

  private class ScriptingServiceScanner extends ObjectScanner<ScriptingServiceImp> {

    @Override
    protected Function<Object, Boolean> objectMatcher() {
      return (object) -> ScriptingServiceImp.class.isAssignableFrom(object.getClass());
    }
  }
}
