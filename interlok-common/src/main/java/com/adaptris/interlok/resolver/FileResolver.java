package com.adaptris.interlok.resolver;

import com.adaptris.annotation.AdvancedConfig;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolver implementation that resolves information from files on the
 * local file system.
 * <p>
 * This resolver resolves values based on the given path:
 * %file{what:path}, where 'what' can be one of several things to
 * resolve:
 *  - data        - The actual file data (currently only supports text
 *                  files as the resolve method returns a String).
 *  - size        - The file size (not meaningful for directories or
 *                  other non-regular files).
 *  - type        - The type of file (regular file, directory, symlink,
 *                  etc).
 *  - date_create - The date of file creation.
 *  - date_modify - The last time the files was modified.
 *  - date_access - The date the file was last accessed.
 *  - permissions - The file permissions (Unix-like; ugo-rwx).
 * </p>
 */
public class FileResolver extends ResolverImp
{
	// Should match %file{data:...}$file{size:...} etc.
	private static final String RESOLVE_REGEXP = "^.*%file\\{([\\w]+):([\\w/\\:\\s\\\"+.]+)\\}.*$";
	private transient Pattern resolverPattern;

	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss zzz";

	@Getter
	@Setter
	@Valid
	@AdvancedConfig(rare = true)
	private String dateFormat;

	public FileResolver()
	{
		resolverPattern = Pattern.compile(RESOLVE_REGEXP);
	}

	/**
	 * Attempt to resolve a value externally.
	 *
	 * @param lookupValue
	 * @return the resolved value
	 */
	@Override
	public String resolve(String lookupValue)
	{
		if (lookupValue == null)
		{
			return null;
		}
		String result = lookupValue;
		log.trace("Resolving {} from filesystem", lookupValue);
		Matcher m = resolverPattern.matcher(lookupValue);
		while (m.matches())
		{
			What what = What.parse(m.group(1));
			File path = new File(m.group(2));
			log.trace("Resolve {} on path {} ", what, path);
			if (what == null)
			{
				log.error("{} is not something that's resolvable", m.group(1));
				throw new UnresolvableException(m.group(1) + " is not something that's resolvable");
			}

			String value = "";
			try
			{
				value = what.resolve(path.toPath());
				if (value == null)
				{
					log.warn("{} resolved to null for {}", what, path);
					value = "";
				}
			}
			catch (IOException e)
			{
				log.error("Could not resolve {} on path {} ", what, path, e);
			}

			String toReplace = "%file{" + what + ":" + path + "}";
			result = result.replace(toReplace, value);
			m = resolverPattern.matcher(result);
		}
		return result;
	}

	/**
	 * Can this resolver handle this type of value.
	 *
	 * @param value the value e.g. {@code %file{url:...;%data;%size;etc}}
	 * @return true or false.
	 */
	@Override
	public boolean canHandle(String value)
	{
		return resolverPattern.matcher(value).matches();
	}

	public enum What
	{
		DATA
		{
			public String resolve(Path path) throws IOException
			{
				if (Files.isRegularFile(path))
				{
					// TODO handle binary data
					return Files.readString(path);
				}
				return null;
			}
		},
		SIZE
		{
			public String resolve(Path path) throws IOException
			{
				if (Files.isRegularFile(path))
				{
					return Long.toString(Files.size(path));
				}
				return null;
			}
		},
		TYPE
		{
			public String resolve(Path path)
			{
				if (Files.isSymbolicLink(path))
				{
					return Type.SYMLINK.name();
				}
				else if (Files.isRegularFile(path))
				{
					return Type.FILE.name();
				}
				else if (Files.isDirectory(path))
				{
					return Type.DIRECTORY.name();
				}
				return null;
			}
		},
		DATE_CREATE
		{
			public String resolve(Path path) throws IOException
			{
				BasicFileAttributes a = Files.readAttributes(path, BasicFileAttributes.class);
				FileTime t = a.creationTime();
				return formatDate(t);
			}
		},
		DATE_ACCESS
		{
			public String resolve(Path path) throws IOException
			{
				BasicFileAttributes a = Files.readAttributes(path, BasicFileAttributes.class);
				FileTime t = a.lastAccessTime();
				return formatDate(t);
			}
		},
		DATE_MODIFY
		{
			public String resolve(Path path) throws IOException
			{
				FileTime t = Files.getLastModifiedTime(path);
				return formatDate(t);
			}
		},
		PERMISSIONS
		{
			public String resolve(Path path) throws IOException
			{
				Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
				StringBuilder sb = new StringBuilder();
				for (PosixFilePermission p : permissions)
				{
					if (sb.length() > 0)
					{
						sb.append(',');
					}
					sb.append(p.name());
				}
				return sb.toString();
			}
		};

		What()
		{
		}

		public abstract String resolve(Path path) throws IOException;

		public static What parse(String s)
		{
			for (What w : What.values())
			{
				if (w.name().equalsIgnoreCase(s))
				{
					return w;
				}
			}
			return null;
		}

		private static String formatDate(FileTime t)
		{
			// TODO make date format user configurable
			Date d = new Date(t.toMillis());
			return new SimpleDateFormat(DATE_FORMAT).format(d);
		}

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}
	}

	public enum Type
	{
		FILE,
		DIRECTORY,
		SYMLINK;
	}
}
