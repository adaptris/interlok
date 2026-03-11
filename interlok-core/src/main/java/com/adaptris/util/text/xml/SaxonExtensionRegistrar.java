package com.adaptris.util.text.xml;

import net.sf.saxon.Configuration;

public interface SaxonExtensionRegistrar {
    /** Register Saxon extension functions with the given configuration.
     * @param config the Saxon Configuration to register extensions with **/
    void register(Configuration config);
}
