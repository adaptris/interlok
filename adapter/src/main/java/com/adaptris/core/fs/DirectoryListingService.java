package com.adaptris.core.fs;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.validation.constraints.NotNull;

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
 * List the contents of a directory.
 *
 * @config ls-to-metadata-service
 */
@AdapterComponent
@ComponentProfile(summary = "List the contents of a directory", tag = "service,list,directory,ls")
@XStreamAlias("directory-listing-service")
public class DirectoryListingService extends ServiceImp
{
	/**
	 * Whether debug mode is enabled.
	 */
	private boolean debugMode = false;

	/**
	 * The metadata key to export file listing data to.
	 */
	private DataInputParameter<String> metadataKey;

	/**
	 * The folder to get a directory listing of.
	 */
	@NotNull
	private DataInputParameter<String> directoryPath;

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void doService(final AdaptrisMessage message) throws ServiceException
	{
		if (directoryPath == null)
		{
			log.error("Directory path is NULL, this service ({}) will not execute.", getUniqueId());
			throw new ServiceException("Missing Required Parameters");
		}

		NumberFormat.getNumberInstance(Locale.UK);
		try
		{
			final String path = directoryPath.extract(message);
			final File directory = new File(path);
			log.trace("ls: {} ", directory.getAbsolutePath());
			if (directory.exists() && directory.isDirectory())
			{
				final StringBuffer buffer = new StringBuffer();
				for (final File file : directory.listFiles())
				{
					log.trace("Found file: {}", file.getName());
					buffer.append(file.getName());
					buffer.append(file.isDirectory() ? "/" : "");
					if (debugMode)
					{
						buffer.append('\t');
						buffer.append(humanReadableByteCount(file.length()));
						buffer.append('\t');
						buffer.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date(file.lastModified())));
					}
					buffer.append('\n');
				}
				setOutput(message, buffer.toString());
			}
			else
			{
				log.warn("Directory does not exist: {}", path);
				setOutput(message, "");
			}
		}
		catch (final InterlokException e)
		{
			log.error(e.getMessage());
			throw new ServiceException(e);
		}
	}

	/**
	 * Depending on whether the metadata key is set, put the directory listing output in the correct place (metadata or payload).
	 * 
	 * @param message
	 *            The message.
	 * @param output
	 *            The directory listing.
	 * 
	 * @throws InterlokException
	 *             Thrown if there is a problem with the metadata key.
	 */
	private void setOutput(final AdaptrisMessage message, final String output) throws InterlokException
	{
		if (metadataKey == null)
		{
			message.setContent(output, message.getContentEncoding());
		}
		else
		{
			message.addMessageHeader(metadataKey.extract(message), output);
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
		if (directoryPath == null)
		{
			log.warn("Directory path is NULL, this service ({}) will not execute.", getUniqueId());
		}
	}

	/**
	 * Get the directory path parameter.
	 *
	 * @return The directory path parameter.
	 */
	public DataInputParameter<String> getDirectoryPath()
	{
		return directoryPath;
	}

	/**
	 * Set the directory path parameter.
	 *
	 * @param directoryPath
	 *            The directory path parameter.
	 */
	public void setDirectoryPath(final DataInputParameter<String> directoryPath)
	{
		this.directoryPath = directoryPath;
	}

	/**
	 * Get the metadata key parameter.
	 *
	 * @return The metadata key parameter.
	 */
	public DataInputParameter<String> getMetadataKey()
	{
		return metadataKey;
	}

	/**
	 * Set the metadata key parameter.
	 *
	 * @param metadataKey
	 *            The metadata key parameter.
	 */
	public void setMetadataKey(final DataInputParameter<String> metadataKey)
	{
		this.metadataKey = metadataKey;
	}

	/**
	 * Check whether debug mode is enabled.
	 *
	 * @return True if debug mode is enabled.
	 */
	protected boolean isDebugMode()
	{
		return debugMode;
	}

	/**
	 * Check whether debug mode is enabled.
	 *
	 * @return True if debug mode is enabled.
	 */
	public Boolean getDebugMode()
	{
		return debugMode;
	}

	/**
	 * Enable/Disable debug mode.
	 *
	 * @param debugMode
	 *            Whether debug mode should be enabled.
	 */
	public void setDebugMode(final Boolean debugMode)
	{
		this.debugMode = debugMode != null ? debugMode : false;
	}

	/**
	 * Get a human readable file size.
	 *
	 * @param bytes
	 *            The file size in bytes.
	 *
	 * @return A String representation of the file size.
	 */
	private static String humanReadableByteCount(final long bytes)
	{
		final int k = 1024;
		if (bytes < k)
		{
			return bytes + " B";
		}
		final int exp = (int)(Math.log(bytes) / Math.log(k));
		return String.format("%.1f %ciB", bytes / Math.pow(k, exp), "KMGTPE".charAt(exp - 1));
	}
}
