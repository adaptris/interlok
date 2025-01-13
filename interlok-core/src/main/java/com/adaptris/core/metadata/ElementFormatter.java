package com.adaptris.core.metadata;

import com.adaptris.core.MetadataElement;

/**
 * Format element metadata as a string.
 *
 * @author Ashley Anderson <ashley.anderson@reedbusiness.com>
 */
public interface ElementFormatter {
	/**
	 * Format the matadata element as a string.
	 *
	 * @param element
	 *          The metadata element.
	 *
	 * @return The string.
	 */
	public String format(MetadataElement element);
}
