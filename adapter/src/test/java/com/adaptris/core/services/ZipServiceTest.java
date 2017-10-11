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

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.GeneralServiceExample;
import com.adaptris.core.ServiceException;
import com.adaptris.core.stubs.DefectiveMessageFactory;

public class ZipServiceTest extends GeneralServiceExample
{

	public ZipServiceTest(final String testName)
	{
		super(testName);
	}

	@Override
	protected void setUp() throws Exception
	{
		/* empty method */
	}

	@Test
	public void testZipDirectoryService() throws Exception
	{
		final AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
		msg.addMetadata("zip-path", "ivy");
		final ZipDirectoryService zip = new ZipDirectoryService();
		zip.setDirectoryPath("%message{zip-path}");
		execute(zip, msg);

		final byte[] zippedData = msg.getPayload();

		msg.setPayload(zippedData);
		execute(new UnzipDirectoryService(), msg);
		final String unzippedPath = msg.getContent();

		System.out.println(unzippedPath);
	}

	public void testZipDirectoryServiceFailure() throws Exception
	{
		final AdaptrisMessage msg = new DefectiveMessageFactory().newMessage();
		msg.addMetadata("zip-path", "ivy");
		try
		{
			final ZipDirectoryService zip = new ZipDirectoryService();
			zip.setDirectoryPath("%message{zip-path}");
			execute(zip, msg);
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
		return new GzipService();
	}

}
