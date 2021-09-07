package com.adaptris.fs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.interlok.resolver.FileResolver;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FileResolverTest
{

	@Test
	public void testResolvablePath()
	{
		AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
		message.addMetadata("file-path", "./build.gradle");

		FileResolver resolver = new FileResolver();

		String s = resolver.resolve("%file{%message{file-path}:%size}", message);
		long size = Long.parseLong(s);
		assertTrue(size > 0);
	}
}
