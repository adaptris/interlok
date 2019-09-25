package com.adaptris.core.services.cache;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;


public abstract class SingleKeyValueCacheImpl extends SingleKeyCacheService {

  @Valid
  @NotNull
  private CacheValueTranslator valueTranslator;

  @Override
  public void prepare() throws CoreException {
    Args.notNull(getValueTranslator(), "valueTranslator");
    super.prepare();
  }

  /**
   * What to do with the cache-value.
   * 
   * @param translator the translator, which depending on the concrete classes has different meanings.
   */
  public void setValueTranslator(CacheValueTranslator translator) {
    valueTranslator = Args.notNull(translator, "valueTranslator");
  }

  /**
   * Get the configured value translator.
   *
   * @return the configured value translator.
   */
  public CacheValueTranslator getValueTranslator() {
    return valueTranslator;
  }

  public <T extends SingleKeyValueCacheImpl> T withValueTranslator(CacheValueTranslator t) {
    setValueTranslator(t);
    return (T) this;
  }
}
