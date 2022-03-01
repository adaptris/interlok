package com.adaptris.logging.log4j2;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.ContextDataInjector;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.impl.ContextDataInjectorFactory;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.ReadOnlyStringMap;

@Plugin(name = "InterlokMgmtComponentFilter", category = Node.CATEGORY, elementType = Filter.ELEMENT_TYPE, printObject = true)
public class InterlokMgmtComponentFilter extends AbstractFilter {

  public static final String CONTEXT_KEY = "ManagementComponent";
  private final ContextDataInjector injector = ContextDataInjectorFactory.createInjector();

  public InterlokMgmtComponentFilter(Result onMatch, Result onMismatch) {
    super(onMatch, onMismatch);
  }

  @PluginBuilderFactory
  public static InterlokMgmtComponentFilter.Builder newBuilder() {
    return new InterlokMgmtComponentFilter.Builder();
  }

  @Override
  public Result filter(final Logger logger, final Level level, final Marker marker, final Message msg,
      final Throwable t) {
    return filter(injector.rawContextData());
  }

  @Override
  public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
      final Object... params) {
    return filter(injector.rawContextData());
  }

  @Override
  public Result filter(final Logger logger, final Level level, final Marker marker, final Object msg,
      final Throwable t) {
    return filter(injector.rawContextData());
  }

  @Override
  public Result filter(final LogEvent event) {
    return filter(event.getContextData());
  }

  private Result filter(ReadOnlyStringMap contextMap) {
    return contextMap.containsKey(CONTEXT_KEY) ? onMatch : onMismatch;
  }

  public static class Builder extends AbstractFilterBuilder<InterlokMgmtComponentFilter.Builder>
      implements org.apache.logging.log4j.core.util.Builder<InterlokMgmtComponentFilter> {

    @Override
    public InterlokMgmtComponentFilter build() {
      return new InterlokMgmtComponentFilter(this.getOnMatch(), this.getOnMismatch());
    }
  }
}
