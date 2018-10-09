package com.adaptris.core.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.*;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.stream.StreamUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.hibernate.validator.constraints.NotBlank;

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
	@Valid
	@InputFieldHint(expression = true)
	private String filePath;

	@AffectsMetadata
	@AdvancedConfig
	private String contentTypeMetadataKey;

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void doService(final AdaptrisMessage message) throws ServiceException
	{
		try
		{
			final File file = new File(message.resolve(getFilePath()));
			if (file.exists() && file.isFile())
			{
				log.info("Reading file : {}", file.getAbsolutePath());
				StreamUtil.copyAndClose(new FileInputStream(file), message.getOutputStream());
				if(getContentTypeMetadataKey() != null) {
					message.addMetadata(getContentTypeMetadataKey(), probeContentType(file));
				}
			}
			else
			{
				log.error("File path does not exist or is a directory : {}", file.getAbsolutePath());
				throw new FileNotFoundException("File " + file.getAbsolutePath() + " does not exist or is a directory!");
			}
		}
		catch (final IOException e)
		{
			log.error(e.getMessage());
			throw new ServiceException(e);
		}
	}

	private String probeContentType(File file) throws IOException {
		String result = Files.probeContentType(file.toPath());
		return result != null ? result : "";
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
		try
		{
			Args.notNull(getFilePath(), "filePath");
		}
		catch (Exception e)
		{
			throw ExceptionHelper.wrapCoreException(e);
		}
	}

	/**
	 * Get the file path parameter.
	 * 
	 * @return The file path parameter.
	 */
	public String getFilePath()
	{
		return filePath;
	}

	/**
	 * Set the file path parameter.
	 * 
	 * @param filePath
	 *            The file path parameter.
	 */
	public void setFilePath(final String filePath)
	{
		this.filePath = Args.notBlank(filePath, "filePath");
	}

	public String getContentTypeMetadataKey() {
		return contentTypeMetadataKey;
	}

	/**
	 * Sets the metadata key set the content type as, if not provided will not be set. (default: null)
	 * @param contentTypeMetadataKey
	 */
	public void setContentTypeMetadataKey(String contentTypeMetadataKey) {
		this.contentTypeMetadataKey =  Args.notBlank(contentTypeMetadataKey, "contentTypeMetadataKey");
	}
}
