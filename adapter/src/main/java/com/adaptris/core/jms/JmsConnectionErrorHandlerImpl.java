package com.adaptris.core.jms;

import static org.apache.commons.lang.StringUtils.abbreviate;
import static org.apache.commons.lang.StringUtils.isEmpty;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.ConnectionErrorHandler;
import com.adaptris.core.ConnectionErrorHandlerImp;
import com.adaptris.core.CoreException;

public abstract class JmsConnectionErrorHandlerImpl extends ConnectionErrorHandlerImp {

  protected transient String idForLogging;

  @Override
  public void init() throws CoreException {
    String loggingId = retrieveConnection(AdaptrisConnection.class).getUniqueId();
    if (!isEmpty(loggingId)) {
      idForLogging = loggingId;
    }
    else {
      idForLogging = abbreviate(retrieveConnection(JmsConnection.class).getBrokerDetailsForLogging(), 20);
    }
  }

  @Override
  public void handleConnectionException() {
    super.restartAffectedComponents();
  }

  @Override
  public boolean allowedInConjunctionWith(ConnectionErrorHandler ceh) {
    return !equals(ceh);
  }

  @Override
  public boolean equals(Object obj) {
    boolean result = false;
    if (obj != null) {
      if (obj instanceof JmsConnectionErrorHandlerImpl) {
        JmsConnection other = ((JmsConnectionErrorHandlerImpl) obj).retrieveConnection(JmsConnection.class);
        JmsConnection thisConnection = retrieveConnection(JmsConnection.class);
        
        result = thisConnection.connectionEquals(other);
      }
    }
    return result;
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    if (retrieveConnection(JmsConnection.class) != null) {
      hashCode = new HashCodeBuilder(11, 17).append(retrieveConnection(JmsConnection.class)).toHashCode();
    }
    return hashCode == 0 ? super.hashCode() : hashCode;
  }
}
