package com.adaptris.core.services.conditional.conditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.conditional.Condition;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

public abstract class ConditionListImpl extends ConditionImpl {

  @NotNull
  @XStreamImplicit
  @Valid
  @AutoPopulated
  private List<Condition> conditions;

  @Override
  public void init() throws CoreException {
    super.init();
    doLifecycle(getConditions(), e -> {
      LifecycleHelper.init(e);
    });
  }

  @Override
  public void start() throws CoreException {
    super.start();
    doLifecycle(getConditions(), e -> {
      LifecycleHelper.start(e);
    });
  }

  @Override
  public void stop() {
    super.stop();
    doLifecycleQuietly(getConditions(), e -> {
      LifecycleHelper.stop(e);
    });
  }

  @Override
  public void close() {
    super.close();
    doLifecycleQuietly(getConditions(), e -> {
      LifecycleHelper.close(e);
    });
  }


  public List<Condition> getConditions() {
    return conditions;
  }

  public void setConditions(List<Condition> conditions) {
    this.conditions = Args.notNull(conditions, "conditions");
  }

  public <T extends ConditionListImpl> T withConditions(Condition... conditions) {
    setConditions(new ArrayList<>(Arrays.asList(conditions)));
    return (T) this;
  }

  protected static void doLifecycle(List<Condition> list, LifecycleOperation p) throws CoreException {
    for (ComponentLifecycle c : list) {
      p.execute(c);
    }
  }

  protected static void doLifecycleQuietly(List<Condition> list, LifecycleOperation p) {
    try {
      doLifecycle(list, p);
    } catch (Exception e) {
      // eat the exception;
    }
  }

  @FunctionalInterface
  protected interface LifecycleOperation {
    void execute(ComponentLifecycle c) throws CoreException;
  }
}
