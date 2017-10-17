package com.adaptris.core.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.validation.constraints.NotNull;

import org.apache.commons.io.FileUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Read a file from a specific path into the message payload.
 *
 * @config read-file-service
 */
@AdapterComponent
@ComponentProfile(summary = "Read a file from a specific path into the message payload", tag = "service,read,file")
@XStreamAlias("read-file-service")
public class ReadFileService extends ServiceImp
{
	private static final String ERROR = "File Path has not been set, this service will not execute.";

	/**
	 * The parameter for the path to the file to read.
	 */
	@NotNull
	private DataInputParameter<String> filePath;

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void doService(final AdaptrisMessage message) throws ServiceException
	{
		if (filePath == null)
		{
			log.error(ERROR);
			throw new ServiceException(ERROR);
		}

		try
		{
			final File file = new File(filePath.extract(message));
			if (file.exists() && file.isFile())
			{
				log.info("Reading file : {}", file.getAbsolutePath());
				final byte[] readFileToByteArray = FileUtils.readFileToByteArray(file);
				message.setPayload(readFileToByteArray);
			}
			else
			{
				log.error("File path does not exist or is a directory : {}", file.getAbsolutePath());
				throw new FileNotFoundException("File " + file.getAbsolutePath() + " does not exist or is a directory!");
			}
		}
		catch (final InterlokException | IOException e)
		{
			throw new ServiceException(e);
		}
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void prepare() throws CoreException
	{
		/* empty method */
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	protected void closeService()
	{
		/* empty method */
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	protected void initService() throws CoreException
	{
		if (filePath == null)
		{
			log.warn(ERROR);
		}
	}

	/**
	 * Get the file path parameter.
	 * 
	 * @return The file path parameter.
	 */
	public DataInputParameter<String> getFilePath()
	{
		return filePath;
	}

	/**
	 * Set the file path parameter.
	 * 
	 * @param filePath
	 *            The file path parameter.
	 */
	public void setFilePath(final DataInputParameter<String> filePath)
	{
		this.filePath = filePath;
	}
}
