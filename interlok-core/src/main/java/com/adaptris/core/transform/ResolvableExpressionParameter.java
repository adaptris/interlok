/*
 * Copyright 2020 Adaptris Ltd.
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

package com.adaptris.core.transform;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairList;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link XmlTransformParameter} implementation that filters resolvable
 * expressions making matches available as String parameters.
 *
 * @author aanderson
 * @config xml-transform-resolvable-expression-parameter
 */
@XStreamAlias("xml-transform-resolvable-expression-parameter")
@DisplayOrder(order = { "expressions" })
public class ResolvableExpressionParameter implements XmlTransformParameter
{
  @NotNull
  @Valid
  @InputFieldHint(expression = true)
  private KeyValuePairList expressions;

  @Override
  public Map<Object, Object> createParameters(AdaptrisMessage message, Map<Object, Object> existingParams)
  {
    if (existingParams == null)
    {
      existingParams = new HashMap<>();
    }
    for (KeyValuePair pair : expressions.getKeyValuePairs())
    {
      existingParams.put(pair.getKey(), message.resolve(pair.getValue()));
    }
    return existingParams;
  }

  public void setExpressions(KeyValuePairList expressions)
  {
    this.expressions = expressions;
  }

  public KeyValuePairList getExpressions()
  {
    return expressions;
  }
}
