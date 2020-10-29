package com.adaptris.core.http.jetty.retry;

import javax.validation.constraints.NotNull;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.util.Args;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * @since 3.11.1
 */
@NoArgsConstructor
public abstract class RetryStoreServiceImpl extends ServiceImp {
  /**
   * Where messages are stored for retries.
   *
   */
  @Getter
  @Setter
  @NotNull
  @NonNull
  private RetryStore retryStore;

  @Override
  public void prepare() throws CoreException {
    Args.notNull(getRetryStore(), "retry-store");
    LifecycleHelper.prepare(getRetryStore());
  }

  @Override
  protected void initService() throws CoreException {
    LifecycleHelper.init(getRetryStore());

  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(getRetryStore());
    super.start();

  }

  @Override
  public void stop() {
    LifecycleHelper.stop(getRetryStore());
    super.stop();

  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(getRetryStore());
  }

  public <T extends RetryStoreServiceImpl> T withRetryStore(RetryStore rs) {
    setRetryStore(rs);
    return (T) this;
  }
}
