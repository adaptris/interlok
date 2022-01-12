package com.adaptris.core;

import com.adaptris.interlok.resolver.ResolverImp;
import com.adaptris.interlok.resolver.UnresolvableException;
import com.adaptris.interlok.types.InterlokMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolver implementation that resolves and escapes XML content.
 * <p>
 * This resolver resolves values based on the following:
 * %asCDATA{...}, and will place the result inside of CDATA tags.
 * </p>
 */
@Slf4j
public class SaferXMLResolver extends ResolverImp
{
	private static final String RESOLVE_REGEXP = "^.*%asCDATA\\{(.+)\\}.*$";
	private final transient Pattern resolverPattern;

	private static final String CDATA_PRE = "<![CDATA[";
	private static final String CDATA_POST = "]]>";
	private static final String SPECIAL_CASE = "<![CDATA[]]]]><![CDATA[>]]>";

	public SaferXMLResolver()
	{
		resolverPattern = Pattern.compile(RESOLVE_REGEXP, Pattern.DOTALL);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public String resolve(String lookupValue)
	{
		throw new UnresolvableException("Safer XML resolver requires a target message!");
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public String resolve(String lookupValue, InterlokMessage target)
	{
		if (target == null)
		{
			throw new UnresolvableException("Target message cannot be null!");
		}
		if (lookupValue == null)
		{
			lookupValue = target.getContent();
		}
		String result = lookupValue;
		log.trace("Resolving {} from XML", lookupValue);
		Matcher m = resolverPattern.matcher(lookupValue);
		while (m.matches())
		{
			String replace = m.group(1);

			StringBuffer sb = new StringBuffer();
			sb.append(CDATA_PRE);

			String value = target.resolve(replace);
			log.trace("Found value {} within target message", value);
			if (value.contains(CDATA_POST))
			{
				// special case for when the resolved text contains its own CDATA section
				value = value.replace(CDATA_POST, SPECIAL_CASE);
			}
			sb.append(value);

			sb.append(CDATA_POST);
			String toReplace = "%asCDATA{" + replace + "}";
			result = result.replace(toReplace, sb.toString());
			m = resolverPattern.matcher(result);
		}
		return result;
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public boolean canHandle(String value)
	{
		return resolverPattern.matcher(value).matches();
	}
}
