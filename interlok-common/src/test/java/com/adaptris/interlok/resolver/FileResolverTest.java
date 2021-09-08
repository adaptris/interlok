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

    String s = resolver.resolve("%file{./build.gradle:%size}");
    long size = Long.parseLong(s);
    assertTrue(size > 0);

    String d = resolver.resolve("%file{./build.gradle:%data}");
    assertEquals(d.length(), size);

    String type = resolver.resolve("%file{./build.gradle:%type}");
    assertEquals(FileResolver.Type.FILE.name(), type);

    String permissions = resolver.resolve("%file{./build.gradle:%permissions}");
    assertTrue(permissions.contains(PosixFilePermission.OWNER_READ.name()));

    String combined = resolver.resolve("%file{./build.gradle:%size%type%permissions}");
    String expected = s + "," + type + "," + permissions;
    assertEquals(expected, combined);

    d = resolver.resolve("%file{./build.gradle:%date_create}");
    assertTrue(d.length() > 0);
    d = resolver.resolve("%file{./build.gradle:%date_modify}");
    assertTrue(d.length() > 0);
    d = resolver.resolve("%file{./build.gradle:%date_access}");
    assertTrue(d.length() > 0);

    // not meaningful to get size or data from a directory (so just the type is resolved)
    type = resolver.resolve("%file{./src:%size%type%data}");
    assertEquals(FileResolver.Type.DIRECTORY.name(), type);

    // non-existent path
    assertTrue(StringUtils.isEmpty(resolver.resolve("%file{./unknown:%type%permissions}")));

    assertNull(resolver.resolve(null));
  }

  @Test(expected = UnresolvableException.class)
  public void testException() throws UnresolvableException
  {
    FileResolver resolver = new FileResolver();

    // unknown resolvable
    resolver.resolve("%file{wrong:%unknown}");
  }

  @Test
  public void testCanHandle()
  {
    FileResolver resolver = new FileResolver();

    assertFalse(resolver.canHandle("hello"));
    assertFalse(resolver.canHandle("%sysprop{java.version}"));

    assertTrue(resolver.canHandle("%file{/some/path:%size}"));
    assertTrue(resolver.canHandle("%file{/some/path:%data}"));
    assertTrue(resolver.canHandle("%file{/some/path:%size%data}"));
  }
}
