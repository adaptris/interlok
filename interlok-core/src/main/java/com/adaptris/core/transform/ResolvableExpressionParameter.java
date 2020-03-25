package com.adaptris.core.transform;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.interlok.InterlokException;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairList;
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
