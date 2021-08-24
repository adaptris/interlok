package com.adaptris.interlok.resolver;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.nio.file.attribute.PosixFilePermission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FileResolverTest
{

	@Test
	public void testResolveString()
	{
		FileResolver resolver = new FileResolver();

		String s = resolver.resolve("%file{size:./build.gradle}");
		long size = Long.parseLong(s);
		assertTrue(size > 0);

		String d = resolver.resolve("%file{data:./build.gradle}");
		assertEquals(d.length(), size);

		String type = resolver.resolve("%file{type:./build.gradle}");
		assertEquals(FileResolver.Type.FILE.name(), type);

		String permissions = resolver.resolve("%file{permissions:./build.gradle}");
		assertTrue(permissions.contains(PosixFilePermission.OWNER_READ.name()));


		d = resolver.resolve("%file{date_create:./build.gradle}");
		assertTrue(d.length() > 0);
		d = resolver.resolve("%file{date_modify:./build.gradle}");
		assertTrue(d.length() > 0);
		d = resolver.resolve("%file{date_access:./build.gradle}");
		assertTrue(d.length() > 0);

		// not meaningful to get size or data from a directory (so just the type is resolved)
		type = resolver.resolve("%file{size:./src}%file{type:./src}%file{data:./src}");
		assertEquals(FileResolver.Type.DIRECTORY.name(), type);

		// non-existent path
		assertTrue(StringUtils.isEmpty(resolver.resolve("%file{type:./unknown}%file{permissions:./unknown}")));

		assertNull(resolver.resolve(null));
	}

	@Test(expected = UnresolvableException.class)
	public void testException() throws UnresolvableException
	{
		FileResolver resolver = new FileResolver();

		// unknown resolvable
		resolver.resolve("%file{wrong:unknown}");
	}

	@Test
	public void testCanHandle()
	{
		FileResolver resolver = new FileResolver();

		assertFalse(resolver.canHandle("hello"));
		assertFalse(resolver.canHandle("%sysprop{java.version}"));

		assertTrue(resolver.canHandle("%file{size:/some/path}"));
		assertTrue(resolver.canHandle("%file{data:/some/path}"));
		assertTrue(resolver.canHandle("%file{type:/some/path}"));
	}
}
