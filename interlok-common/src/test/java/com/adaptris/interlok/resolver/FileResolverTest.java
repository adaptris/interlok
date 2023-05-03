package com.adaptris.interlok.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.attribute.PosixFilePermission;

import org.junit.jupiter.api.Test;

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

    String permissions = "";
    if (!System.getProperty("os.name").contains("Windows"))
    {
      permissions = resolver.resolve("%file{./build.gradle:%permissions}");
      assertTrue(permissions.contains(PosixFilePermission.OWNER_READ.name()));
    }

    String combined = resolver.resolve("%file{./build.gradle:%size%type%permissions}");
    String expected = s + "," + type + (permissions.length() > 0 ? "," + permissions : "");
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


    assertNull(resolver.resolve(null));
  }

  @Test
  public void testException()
  {
    FileResolver resolver = new FileResolver();

    try
    {
      // unknown resolvable
      resolver.resolve("%file{./build.gradle:%unknown}");
      fail();
    }
    catch (UnresolvableException e)
    {
      // expected
    }
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
