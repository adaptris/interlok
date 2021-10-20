package com.adaptris.core.services.conditional.conditions;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.MarshallingCDATA;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A javascript condition.
 *
 * <p>
 * This makes use of the {@link Invocable} extension to {@link ScriptEngine}, to allow you to define
 * the function that will be executed to evaluate the condition. The function name should always be
 * {@code evaluateScript}; take a single parameter (in this case it will be the current
 * {@link AdaptrisMessage}; and return {@code true or false}. For instance to check a specific
 * metadata value then you might have this function definition
 *
 * <pre>
 * {@code
     function evaluateScript(message) {
       return message.getMetadataValue('myMetadataKey').equals('myValue');
     }
 * }
 * </pre>
 * </p>
 * <p>
 * Similar to {@link com.adaptris.core.services.EmbeddedScriptingService}; the logger is bound as
 * {@code log}.
 * </p>
 *
 * @config function
 */
@XStreamAlias("function")
@AdapterComponent
@ComponentProfile(summary = "Condition that makes use of the built in nashorn scripting engine for condition evaluation", tag = "condition,service")
public class ConditionFunction extends ConditionImpl {

  private transient ScriptEngine engine = null;

  @MarshallingCDATA
  private String definition;

  public ConditionFunction() {

  }

  public ConditionFunction(String func) {
    this();
    setDefinition(func);
  }

  @Override
  public boolean evaluate(AdaptrisMessage msg) throws ServiceException {
    try {
      Object o = ((Invocable) engine).invokeFunction("evaluateScript", msg);
      logCondition("{}: evaluated to : {}", getClass().getSimpleName(), o.toString());
      return BooleanUtils.toBoolean(o.toString());
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void init() throws CoreException {
    try {
      ScriptEngineManager  manager = new ScriptEngineManager(this.getClass().getClassLoader());
      engine = Args.notNull(manager.getEngineByName("nashorn"), "nashorn engine");
      engine.eval(definition);
      Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
      bindings.put("log", log);
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  public String getDefinition() {
    return definition;
  }

  public void setDefinition(String f) {
    this.definition = f;
  }
}
