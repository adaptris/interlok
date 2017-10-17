/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.core.services;

import java.io.File;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.GeneralServiceExample;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.ConstantDataInputParameter;

public class LsToMetadataServiceTest extends GeneralServiceExample
{
	private static final String DIRECTORY_PATH = "ivy";
	private static final String METADATA_KEY = "service-test";

	public LsToMetadataServiceTest(final String testName)
	{
		super(testName);
	}

	@Override
	protected void setUp() throws Exception
	{
		/* empty method */
	}

	@Test
	public void testService() throws Exception
	{
		final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
		final LsToMetadataService service = (LsToMetadataService)retrieveObjectForSampleConfig();

		execute(service, message);

		final String ls = message.getMetadataValue(METADATA_KEY);

		for (final File file : new File(DIRECTORY_PATH).listFiles())
		{
			assertTrue(ls.contains(file.getName()));
		}
	}

	@Test
	public void testDebugMode() throws Exception
	{
		final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
		final LsToMetadataService service = (LsToMetadataService)retrieveObjectForSampleConfig();
		service.setDebugMode(true);

		execute(service, message);

		final String ls = message.getMetadataValue(METADATA_KEY);

		for (final File file : new File(DIRECTORY_PATH).listFiles())
		{
			assertTrue(ls.contains(file.getName()));
		}
	}
	
	@Test
	public void testServiceFailedInit() throws Exception
	{
		try
		{
			final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
			final LsToMetadataService service = new LsToMetadataService();

			execute(service, message);
			fail();
		}
		catch (@SuppressWarnings("unused") final ServiceException expected)
		{
			/* expected result */
		}
	}

	@Override
	protected Object retrieveObjectForSampleConfig()
	{
		final LsToMetadataService service =  new LsToMetadataService();
		service.setDirectoryPath(new ConstantDataInputParameter(DIRECTORY_PATH));
		service.setMetadataKey(new ConstantDataInputParameter(METADATA_KEY));
		return service;
	}
}
