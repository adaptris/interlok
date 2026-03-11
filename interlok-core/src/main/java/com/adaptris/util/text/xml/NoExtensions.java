package com.adaptris.util.text.xml;

import net.sf.saxon.Configuration;

/**
 * Null implementation of {@link SaxonExtensionRegistrar} that performs no operations.
 */
public class NoExtensions implements SaxonExtensionRegistrar {

    @Override
    public void register(Configuration config) {
        // No-op implementation
    }
}
