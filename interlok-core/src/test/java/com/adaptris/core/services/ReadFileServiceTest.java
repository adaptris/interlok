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

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.GeneralServiceExample;

public class ReadFileServiceTest extends GeneralServiceExample
{
	private static final String FILE = "build.gradle";

	public ReadFileServiceTest(final String testName)
	{
		super(testName);
	}

	@Override
	protected void setUp() throws Exception
	{
		/* empty method */
	}


  @Test
  public void testProbeContentType() throws Exception {
    final ReadFileService service = new ReadFileService();
    assertNotNull(service.contentTypeProbe());
    ReadFileService.ContentTypeProbe myProbe = e-> { return Files.probeContentType(e.toPath()); };
    service.setContentTypeProbe(myProbe);
    assertEquals(myProbe, service.getContentTypeProbe());
    assertEquals(myProbe, service.contentTypeProbe());
  }
  
	@Test
	public void testService() throws Exception
	{
		final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
		final ReadFileService service = retrieveObjectForSampleConfig();

		execute(service, message);

		final byte[] actual = message.getPayload();

		final File file = new File(FILE);
		final byte[] expected = new byte[(int)file.length()];
		try (final FileInputStream fir = new FileInputStream(file))
		{
			fir.read(expected);
		}

		assertArrayEquals(expected, actual);
	}

	 
	@Test
	public void testServiceContentType() throws Exception
	{
		final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
		final ReadFileService service = new ReadFileService();
		service.setFilePath(PROPERTIES.getProperty("XmlTransformService.outputTestMessage"));
		service.setContentTypeMetadataKey("contentType");

		execute(service, message);

		final byte[] actual = message.getPayload();

		final File file = new File(PROPERTIES.getProperty("XmlTransformService.outputTestMessage"));
		final byte[] expected = new byte[(int)file.length()];
		try (final FileInputStream fir = new FileInputStream(file))
		{
			fir.read(expected);
		}

		assertArrayEquals(expected, actual);
		assertTrue(message.containsKey("contentType"));
		// Macs don't seem to be able to probe content type by default.
		if (StringUtils.isNotBlank(message.getMetadataValue("contentType"))) {
		  assertTrue(message.getMetadataValue("contentType").endsWith("/xml"));
		}
	}

  @Test
  public void testService_NoContentType() throws Exception {
    final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    final ReadFileService service = new ReadFileService();
    service.setFilePath(PROPERTIES.getProperty("XmlTransformService.outputTestMessage"));
    execute(service, message);
    final byte[] actual = message.getPayload();
    final File file = new File(PROPERTIES.getProperty("XmlTransformService.outputTestMessage"));
    final byte[] expected = new byte[(int) file.length()];
    try (final FileInputStream fir = new FileInputStream(file)) {
      fir.read(expected);
    }
    assertArrayEquals(expected, actual);
  }
  
	@Test
	public void testServiceFailedInit() throws Exception
	{
		try
		{
			final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
			final ReadFileService service = new ReadFileService();

			execute(service, message);
			fail();
		}
    catch (@SuppressWarnings("unused") final CoreException expected)
		{
			/* expected result */
		}
	}

	@Override
	protected ReadFileService retrieveObjectForSampleConfig()
	{
		final ReadFileService service = new ReadFileService();
    service.setFilePath(FILE);
		return service;
	}
}
