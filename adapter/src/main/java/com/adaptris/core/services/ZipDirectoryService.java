package com.adaptris.core.services;

import java.io.IOException;
import java.io.OutputStream;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ZipFolder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Zip a directory and put the compressed data in the payload.
 *
 * @config zip-service
 */
@AdapterComponent
@ComponentProfile(summary = "Zip the contents of the message", tag = "service,zip")
@XStreamAlias("zip-service")
public class ZipDirectoryService extends ServiceImp
{
	/**
	 * The directory to zip.
	 */
	private String directoryPath;

	/**
	 * Get the directory to zip.
	 * 
	 * @return The directory to zip.
	 */
	public String getDirectoryPath()
	{
		return directoryPath;
	}

	/**
	 * Set the directory to zip.
	 * 
	 * @param directoryPath
	 *            The directory to zip.
	 */
	public void setDirectoryPath(final String directoryPath)
	{
		this.directoryPath = directoryPath;
	}

	@Override
	public void doService(final AdaptrisMessage msg) throws ServiceException
	{
		final ZipFolder zipFolder = new ZipFolder(msg.resolve(directoryPath));
		msg.setContent(null, null);
		try (final OutputStream outputStream = msg.getOutputStream())
		{
			final byte[] zippedData = zipFolder.zip();
			outputStream.write(zippedData);
			outputStream.flush();
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
