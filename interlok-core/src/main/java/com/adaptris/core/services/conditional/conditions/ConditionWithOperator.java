package com.adaptris.core.services.conditional.conditions;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.core.services.conditional.Operator;
import com.adaptris.core.util.Args;

public abstract class ConditionWithOperator extends ConditionImpl {

  @NotNull
  @Valid
  private Operator operator;

  public ConditionWithOperator() {
  }

  public Operator getOperator() {
    return operator;
  }

  /**
   * Set the operators to apply.
   * 
   * 
   * @param oper the operators
   */
  public void setOperator(Operator oper) {
    this.operator = Args.notNull(oper, "operator");
  }

  protected Operator operator() {
    return Args.notNull(getOperator(), "operator");
  }
}
