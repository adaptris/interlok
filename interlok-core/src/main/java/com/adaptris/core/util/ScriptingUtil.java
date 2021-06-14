package com.adaptris.core.util;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.script.ScriptEngineManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScriptingUtil {

  static final String NASHORN_ENGINE = "Oracle Nashorn";
  static final String GRAAL_JS_ENGINE = "Graal.js";
  static final String NASHORN = "Nashorn";

  /**
   * Checks if the script variant requested will end up using nashorn or not.
   * <p>
   * Since Java 17 will remove the nashorn scripting engine, we need to have a check in place to
   * detect that.
   * </p>
   *
   * @param engineManager the engineManager
   * @param lang the language
   * @return true if we're going to end up using nashorn.
   */
  public static final boolean dependsOnNashorn(ScriptEngineManager engineManager, String lang) {
    Args.notBlank(lang, "scripting-language");
    Args.notNull(engineManager, "scripting-engine");
    if (nashornNames(engineManager).contains(lang)) {
      // Nashorn engine javascript short names ("js", "JS", "JavaScript", "javascript",
      // "ECMAScript", "ecmascript") are also used by GraalJS via -Dpolyglot.js.nashorn-compat=true
      // so we only warn the user if he uses directly nashorn or if GraalJS can not be found.
      if (isNashorn(lang) || !hasGraalJsEngine(engineManager)) {
        return true;
      }
    }
    return false;
  }


  private static List<String> nashornNames(ScriptEngineManager engineManager) {
    // List of short names which may be used to identify the Nashorn ScriptEngine.
    return engineManager.getEngineFactories().stream()
        .filter(e -> NASHORN_ENGINE.equals(e.getEngineName())).flatMap(e -> e.getNames().stream())
        .collect(Collectors.toList());
  }

  private static boolean isNashorn(String language) {
    return NASHORN.equalsIgnoreCase(language);
  }

  private static boolean hasGraalJsEngine(ScriptEngineManager engineManager) {
    return Optional.ofNullable(engineManager.getEngineByName(GRAAL_JS_ENGINE)).isPresent();
  }
}
