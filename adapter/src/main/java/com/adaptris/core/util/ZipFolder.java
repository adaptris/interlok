package com.adaptris.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zip or unzip a file or directory.
 * 
 * @author andersonam
 */
public class ZipFolder
{
	private static final int ONE_MEG = 1024 * 1024;

	private static final Logger LOG = LoggerFactory.getLogger(ZipFolder.class);

	/**
	 * The folder to zip.
	 */
	private String folder;

	/**
	 * List of files included in the zip file.
	 */
	private final List<String> fileList = new ArrayList<>();

	/**
	 * Create a new instance, with the given folder as either the folder to zip from to to.
	 * 
	 * @param folder
	 *            The source/target folder.
	 */
	public ZipFolder(final String folder)
	{
		this.folder = folder;
	}

	/**
	 * Zip the folder and return the compressed data.
	 * 
	 * @return The compressed data.
	 * 
	 * @throws IOException
	 *             Thrown if the source folder doesn't exist or there is a problem during compression.
	 */
	public byte[] zip() throws IOException
	{
		final File zipRoot = new File(folder);
		if (!zipRoot.exists())
		{
			LOG.error("Attempting to ZIP nonexistant directory!");
			throw new FileNotFoundException("Attempting to ZIP nonexistant directory!");
		}
		final String source;
		if (zipRoot.isFile())
		{
			LOG.info("Compressing single file : " + zipRoot);
			folder = zipRoot.getParent();
			fileList.add(zipRoot.getName());
			source = folder;
		}
		else
		{
			source = zipRoot.getName();
			generateFileList(zipRoot);
		}

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (final ZipOutputStream zos = new ZipOutputStream(baos))
		{
			for (final String filename : fileList)
			{
				zos.putNextEntry(new ZipEntry(new File(source, filename).getPath()));
				final File file = new File(folder, filename);
				try (final FileInputStream in = new FileInputStream(file))
				{
					LOG.debug("Add " + file.getPath() + " to ZIP archive");
					final byte[] buffer = new byte[ONE_MEG];
					int len;
					while ((len = in.read(buffer)) > 0)
					{
						zos.write(buffer, 0, len);
					}
				}
			}
			zos.closeEntry();
		}
		return baos.toByteArray();
	}

	/**
	 * Unzip the data to the target directory.
	 * 
	 * @param data
	 *            The compressed data to unzip.
	 * 
	 * @throws IOException
	 *             Thrown if there is problem during decompression
	 */
	public void unzip(final byte[] data) throws IOException
	{
		try (final ByteArrayInputStream bais = new ByteArrayInputStream(data))
		{
			unzip(bais);
		}
	}

	/**
	 * Unzip the data to the target directory.
	 * 
	 * @param zipStream
	 *            The compressed data to unzip.
	 * 
	 * @throws IOException
	 *             Thrown if there is problem during decompression
	 */
	public String unzip(final InputStream zipStream) throws IOException
	{
		final File dir = new File(folder);
		// create output directory if it doesn't exist
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		// buffer for read and write data to file
		try (final ZipInputStream zis = new ZipInputStream(zipStream))
		{
			final byte[] buffer = new byte[ONE_MEG];

			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null)
			{
				final String fileName = ze.getName();

				final File file = new File(folder, fileName);
				// create directories for sub directories in zip
				new File(file.getParent()).mkdirs();

				if (!ze.isDirectory())
				{
					try (final FileOutputStream fos = new FileOutputStream(file))
					{
						int len;
						while ((len = zis.read(buffer)) > 0)
						{
							fos.write(buffer, 0, len);
						}
					}
				}
				// close this ZipEntry
				zis.closeEntry();
			}
		}
		return folder;
	}

	/**
	 * Append a file or directory hierarchy to the zip index.
	 * 
	 * @param file
	 *            The file/directory to add to the zip index.
	 */
	private void generateFileList(final File file)
	{
		if (file.isFile())
		{
			fileList.add(file.toString().substring(folder.length() + 1, file.toString().length()));
		}
		if (file.isDirectory())
		{
			for (final String filename : file.list())
			{
				generateFileList(new File(file, filename));
			}
		}
	}
}
