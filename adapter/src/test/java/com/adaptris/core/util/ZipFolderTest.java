package com.adaptris.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ZipFolderTest
{
	private File tmpDir;

	@Before
	public void init()
	{
		tmpDir = new File(System.getProperty("java.io.tmpdir"), "ZipFolderTest");
		if (!tmpDir.exists())
		{
			tmpDir.mkdir();
		}
	}

	@After
	public void deinit()
	{
		for (final File f : tmpDir.listFiles())
		{
			f.delete();
		}
		tmpDir.delete();
	}

	@Test
	public void test1() throws Exception
	{
		ZipFolder zf = new ZipFolder("build.xml");
		final byte[] z = zf.zip();

		final File zip = new File(tmpDir, "build.zip");
		try (final FileOutputStream fos = new FileOutputStream(zip))
		{
			fos.write(z);
		}

		zf = new ZipFolder(tmpDir.getAbsolutePath());
		try (final FileInputStream fis = new FileInputStream(zip))
		{
			zf.unzip(fis);
		}
	}

	@Test
	public void test2() throws Exception
	{
		ZipFolder zf = new ZipFolder(new File("build.xml").getAbsolutePath());
		final byte[] z = zf.zip();

		final File zip = new File(tmpDir, "build.zip");
		try (final FileOutputStream fos = new FileOutputStream(zip))
		{
			fos.write(z);
		}

		zf = new ZipFolder(tmpDir.getAbsolutePath());
		try (final FileInputStream fis = new FileInputStream(zip))
		{
			zf.unzip(fis);
		}
	}
}
