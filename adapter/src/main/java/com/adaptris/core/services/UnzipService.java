package com.adaptris.core.services;

import java.io.File;
import java.io.IOException;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ZipFolder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Unzip a file and set the path to the root directory of the unzipped data in the payload.
 *
 * @config unzip-service
 */
@AdapterComponent
@ComponentProfile(summary = "Zip the contents of the message", tag = "service,zip,unzip")
@XStreamAlias("unzip-service")
public class UnzipService extends ServiceImp
{
	@Override
	public void doService(final AdaptrisMessage msg) throws ServiceException
	{
		final File messageTempDirectory = new File(System.getProperty("java.io.tmpdir"), msg.getUniqueId());
		if (messageTempDirectory.exists())
		{
			throw new ServiceException(messageTempDirectory + " already exists.");
		}

		messageTempDirectory.mkdir();

		final ZipFolder zipFolder = new ZipFolder(messageTempDirectory.getAbsolutePath());
		try
		{
			final String unzippedDirPath = zipFolder.unzip(msg.getInputStream());
			msg.setContent(unzippedDirPath, msg.getContentEncoding());
		}
		catch (final IOException e)
		{
			throw new ServiceException(e);
		}

	}

	@Override
	public void prepare() throws CoreException
	{
		/* empty method */
	}

	@Override
	protected void initService() throws CoreException
	{
		/* empty method */
	}

	@Override
	protected void closeService()
	{
		/* empty method */
	}
}
