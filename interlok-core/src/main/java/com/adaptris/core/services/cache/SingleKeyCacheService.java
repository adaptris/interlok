package com.adaptris.core.services.cache;

import javax.validation.constraints.NotBlank;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;

public abstract class SingleKeyCacheService extends CacheServiceImpl {
  @InputFieldHint(expression = true)
  @NotBlank
  private String key;

  @Override
  public void prepare() throws CoreException {
    Args.notBlank(getKey(), "key");
    super.prepare();
  }

  public <T extends SingleKeyCacheService> T withKey(String s) {
    setKey(s);
    return (T) this;
  }

  public String getKey() {
    return key;
  }

  /**
   * Set the cache key.
   * 
   * @param key the key.
   */
  public void setKey(String key) {
    this.key = Args.notBlank(key, "key");
  }
}
