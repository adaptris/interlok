package com.adaptris.core.management.config;

import com.adaptris.core.management.AdapterConfigManager;

/**
 * The interface which has all the methods that provide the functionality to create, save or sync the adapter.
 * 
 * @deprecated {@link AdapterConfigManager} is now available, the only reason why this still exists is to avoid breaking sonic-mf
 * 
 */
@Deprecated
public interface ConfigManager extends AdapterConfigManager {

}
