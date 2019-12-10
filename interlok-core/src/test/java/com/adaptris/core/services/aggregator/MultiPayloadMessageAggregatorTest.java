package com.adaptris.core.services.aggregator;

import com.adaptris.core.Service;
import com.adaptris.core.services.splitter.MultiPayloadMessageSplitter;

import java.util.List;

public class MultiPayloadMessageAggregatorTest extends AggregatorCase
{
	public MultiPayloadMessageAggregatorTest(String name)
	{
		super(name);
	}

	@Override
	protected MessageAggregatorImpl createAggregatorForTests()
	{
		return new MultiPayloadMessageAggregator();
	}

	@Override
	protected List<Service> retrieveObjectsForSampleConfig()
	{
		return createExamples(new MultiPayloadMessageSplitter(), new MultiPayloadMessageAggregator());
	}
}
