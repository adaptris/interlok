package com.adaptris.core.fs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.validation.constraints.NotBlank;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.CoreConstants;
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
        CoreConstants.ORIGINAL_NAME_KEY, CoreConstants.FS_FILE_SIZE,
        CoreConstants.FILE_LAST_MODIFIED_KEY, CoreConstants.FS_CONSUME_DIRECTORY,
        CoreConstants.MESSAGE_CONSUME_LOCATION, CoreConstants.FS_CONSUME_PARENT_DIR
}, recommended =
{
	NullConnection.class
})
@DisplayOrder(order =
{
    "baseDirectoryUrl", "processedPath", "poller", "createDirs", "filterExpression",
    "fileFilterImp", "fileSorter", "processedItemCache"
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

	/**
	 * {@inheritDoc}.
	 */
  @Override
  protected int processFile(final File file) throws CoreException {
    log.info("Processing file : " + file.getAbsolutePath());
    final int x = super.processFile(file);

    final File dir = new File(processedPath);
    if (dir.exists() && !dir.isDirectory()) {
      throw new CoreException("Processed directory expected; but it wasn't!");
    }
    if (!dir.exists()) {
      log.debug("Creating processed directory : {}", dir.getAbsolutePath());
      dir.mkdirs();
    }
    try {
      final File copy = new File(dir, file.getName());
      Files.copy(file.toPath(), copy.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
      log.info("Copied file [{}] to [{}]", file.getName(), dir.getAbsolutePath());
    } catch (final IOException e) {
      throw new CoreException(e);
    }
    // f.delete(); // maybe we want to delete the files after copying them, maybe we don't

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
