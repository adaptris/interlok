package com.adaptris.core.fs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Extends the non-deleting FS consumer, and after processing stores files in
 * a separate directory. This is similar to the FTP consumer.
 * 
 * {@docRoot}.
 */
@XStreamAlias("moving-non-deleting-fs-consumer")
@AdapterComponent
@ComponentProfile(summary = "Pickup messages from the filesystem and move them afterwards", tag = "consumer,fs,filesystem", metadata =
{
	"originalname", "lastmodified", "fsFileSize", "fsConsumeDir", "fsParentDir"
}, recommended =
{
	NullConnection.class
})
@DisplayOrder(order =
{
	"poller", "createDirs", "fileFilterImp", "fileSorter", "processedItemCache"
})

public class MovingNonDeletingFsConsumer extends NonDeletingFsConsumer
{
	private static final String DEFAULT_PROCESSED_PATH = "proc";

	@NotBlank
	@AutoPopulated
	@AdvancedConfig
	private String processedPath;

	public MovingNonDeletingFsConsumer()
	{
		super();
		setProcessedPath(DEFAULT_PROCESSED_PATH);
	}

	public MovingNonDeletingFsConsumer(final ConsumeDestination d)
	{
		this();
		setDestination(d);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	protected int processFile(final File f) throws CoreException
	{
		final int x = super.processFile(f);

		final File dir = new File(processedPath);
		if (dir.exists() && !dir.isDirectory())
		{
			throw new CoreException("Target directory expected; incorrect target found!");
		}
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		try
		{
			Files.copy(f.toPath(), dir.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
		}
		catch (final IOException e)
		{
			throw new CoreException(e);
		}
		f.delete();

		return x;
	}

	/**
	 * Set where processed file are placed once done with.
	 * 
	 * @param s
	 *            Set the processed path.
	 */
	public void setProcessedPath(final String s)
	{
		processedPath = Args.notBlank(s, "processed path");
	}

	/**
	 * Get the path where processed files are placed once done with.
	 * 
	 * @return Get the processed path.
	 */
	public String getProcessedPath()
	{
		return processedPath;
	}
}
