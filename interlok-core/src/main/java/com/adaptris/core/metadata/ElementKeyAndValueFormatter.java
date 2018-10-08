package com.adaptris.core.metadata;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Key and value element formatter.
 *
 * @author Ashley Anderson <ashley.anderson@reedbusiness.com>
 */
@XStreamAlias("element-key-and-value-formatter")
public class ElementKeyAndValueFormatter implements ElementFormatter {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(ElementKeyAndValueFormatter.class.getName());

	/**
	 * The default key/value separator.
	 */
	private static final String DEFAULT_SEPARATOR = "=";

	/**
	 * The key/value separator.
	 */
	@NotNull
	@AutoPopulated
	@InputFieldDefault(value = DEFAULT_SEPARATOR)
  @InputFieldHint(style = "BLANKABLE")
	private String separator;

	/**
	 * Default constructor.
	 */
	public ElementKeyAndValueFormatter() {
		separator = DEFAULT_SEPARATOR;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String format(final MetadataElement element) {
		if (element == null) {
			LOGGER.warn("Metadata element is null!");
			return null;
		}
		return element.getKey() + separator + element.getValue();
	}

	/**
	 * Set the key/value separator.
	 *
	 * @param separator
	 *          The key/value separator.
	 */
	public void setSeparator(final String separator) {
		this.separator = separator;
	}

	/**
	 * Get the key/value separator.
	 *
	 * @return The key/value separator.
	 */
	public String getSeparator() {
		return separator;
	}
}
