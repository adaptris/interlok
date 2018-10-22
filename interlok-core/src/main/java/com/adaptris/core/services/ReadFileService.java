package com.adaptris.core.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.stream.StreamUtil;
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
	@Valid
	@InputFieldHint(expression = true)
	private String filePath;

	@AffectsMetadata
	@AdvancedConfig
	@InputFieldDefault(value = "null")
	private String contentTypeMetadataKey;

	@AdvancedConfig
	@Valid
	@InputFieldDefault(value = "Files.probeContentType(Path)")
	private ContentTypeProbe contentTypeProbe;

	@Override
	public void doService(final AdaptrisMessage message) throws ServiceException
	{
		try
		{
			final File file = new File(message.resolve(getFilePath()));
			if (file.exists() && file.isFile())
			{
				log.info("Reading file : {}", file.getAbsolutePath());
        try (FileInputStream in = new FileInputStream(file); OutputStream out = message.getOutputStream()) {
          StreamUtil.copyAndClose(in, out);
          if (StringUtils.isNotBlank(getContentTypeMetadataKey())) {
            message.addMetadata(getContentTypeMetadataKey(), probeContentType(file));
          }
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
		  throw ExceptionHelper.wrapServiceException(e);
		}
	}

	private String probeContentType(File file) throws IOException {
	  return StringUtils.defaultIfBlank(contentTypeProbe().probeContentType(file), "");
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
	
	 
  public ContentTypeProbe getContentTypeProbe() {
    return contentTypeProbe;
  }

  public void setContentTypeProbe(ContentTypeProbe contentTypeProbe) {
    this.contentTypeProbe = contentTypeProbe;
  }
  
  protected ContentTypeProbe contentTypeProbe() {
    return getContentTypeProbe() != null ? getContentTypeProbe() : e -> { return Files.probeContentType(e.toPath()); };
  }

  @FunctionalInterface
	public interface ContentTypeProbe {
	  String probeContentType(File f) throws IOException ;
	}
}
