package com.adaptris.core.transform;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.interlok.InterlokException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XStreamAlias("xml-transform-resolvable-expression-parameter")
@DisplayOrder(order = {"expressions"})
public class ResolvableExpressionTransformParameter implements XmlTransformParameter
{
	@NotNull
	@Valid
	@InputFieldHint(expression = true)
	private List<String> expressions;

	@Override
	public Map<Object, Object> createParameters(AdaptrisMessage message, Map<Object, Object> existingParams)
	{
		if (expressions.size() == 0)
		{
			return null;
		}
		if (existingParams == null)
		{
			existingParams = new HashMap<>();
		}
		for (String expression : expressions)
		{
			existingParams.put(expression, message.resolve(expression));
		}
		return existingParams;
	}

	public void setExpressions(List<String> expressions)
	{
		this.expressions = expressions;
	}

	public List<String> getExpressions()
	{
		return expressions;
	}
}
