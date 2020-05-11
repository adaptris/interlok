package com.adaptris.core.http.oauth;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;

public abstract class OauthTokenGetter extends ServiceImp {
  @InputFieldDefault(value = "oauth-access-token-to-metadata")
  @Valid
  private AccessTokenWriter accessTokenWriter;

  @NotNull
  @Valid
  private AccessTokenBuilder accessTokenBuilder;

  private transient AccessTokenWriter tokenWriterToUse;

  public OauthTokenGetter() {

  }


  @Override
  public void prepare() throws CoreException {
    Args.notNull(getAccessTokenBuilder(), "accessTokenBuilder");
    LifecycleHelper.prepare(getAccessTokenBuilder());
    tokenWriterToUse = tokenWriterIfNull();
    LifecycleHelper.prepare(tokenWriterToUse);
  }

  protected AccessTokenWriter tokenWriterIfNull() {
    return ObjectUtils.defaultIfNull(getAccessTokenWriter(), new MetadataAccessTokenWriter());
  }

  protected AccessTokenWriter tokenWriterToUse() {
    return tokenWriterToUse;
  }

  @Override
  protected void initService() throws CoreException {
    LifecycleHelper.init(getAccessTokenBuilder());
    LifecycleHelper.init(tokenWriterToUse);
  }

  @Override
  public void start() throws CoreException {
    super.start();
    LifecycleHelper.start(getAccessTokenBuilder());
    LifecycleHelper.start(tokenWriterToUse);
  }

  @Override
  public void stop() {
    super.stop();
    LifecycleHelper.stop(getAccessTokenBuilder());
    LifecycleHelper.stop(tokenWriterToUse);
  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(getAccessTokenBuilder());
    LifecycleHelper.close(tokenWriterToUse);
  }


  public <T extends OauthTokenGetter> T withAccessTokenBuilder(AccessTokenBuilder b) {
    setAccessTokenBuilder(b);
    return (T) this;
  }

  public AccessTokenBuilder getAccessTokenBuilder() {
    return accessTokenBuilder;
  }


  /**
   * Set the access token builder.
   * 
   * @param b the builder.
   */
  public void setAccessTokenBuilder(AccessTokenBuilder b) {
    this.accessTokenBuilder = Args.notNull(b, "accessTokenBuilder");
  }


  public <T extends OauthTokenGetter> T withAccessTokenWriter(AccessTokenWriter b) {
    setAccessTokenWriter(b);
    return (T) this;
  }

  public AccessTokenWriter getAccessTokenWriter() {
    return accessTokenWriter;
  }


  /**
   * Specify how to write the access token once it is retrieved.
   * 
   * @param b the writer.
   */
  public void setAccessTokenWriter(AccessTokenWriter b) {
    this.accessTokenWriter = b;
  }


}
