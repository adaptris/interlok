package com.adaptris.fs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.interlok.resolver.FileResolver;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class FileResolverTest
{

	@Test
	public void testResolvablePath() throws IOException
	{
		AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
		message.addMetadata("file-path", "./build.gradle");

		FileResolver resolver = new FileResolver();
		String s = resolver.resolve("%file{%message{file-path}:%size}", message);
		long size = Long.parseLong(s);

		assertEquals(Files.size(new File("./build.gradle").toPath()), size);
	}

	@Test
	public void testChainingResolvers() throws IOException
	{
		AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
		message.addMetadata("file-path", "./build.gradle");

		String s = message.resolve("%message{%uniqueId}::%file{%message{file-path}:%size}");
		String x = message.getUniqueId() + "::" + Files.size(new File("./build.gradle").toPath());

		assertEquals(x, s);
	}
}
