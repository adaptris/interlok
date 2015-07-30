package com.adaptris.core.jms.activemq;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.filter.DestinationMapEntry;
import org.apache.activemq.security.AuthenticationUser;
import org.apache.activemq.security.AuthorizationEntry;
import org.apache.activemq.security.AuthorizationPlugin;
import org.apache.activemq.security.DefaultAuthorizationMap;
import org.apache.activemq.security.SimpleAuthenticationPlugin;

public class RequiresCredentialsBroker extends EmbeddedActiveMq {

  public static final String DEFAULT_ROLES = "users";
  public static final String DEFAULT_PREFIX = "example.";
  public static final String DEFAULT_USERNAME = "system";
  public static final String DEFAULT_PASSWORD = "manager";

  public RequiresCredentialsBroker() throws Exception {
    super();
  }

  @Override
  public BrokerService createBroker() throws Exception {
    BrokerService br = super.createBroker();
    SimpleAuthenticationPlugin sap = new SimpleAuthenticationPlugin();
    sap.setUsers(new ArrayList<AuthenticationUser>(Arrays.asList(new AuthenticationUser[]
    {
        new AuthenticationUser(DEFAULT_USERNAME, DEFAULT_PASSWORD, DEFAULT_ROLES),
    })));
    AuthorizationPlugin ap = new AuthorizationPlugin();
    ap.setMap(new DefaultAuthorizationMap(new ArrayList<DestinationMapEntry>(Arrays.asList(new AuthorizationEntry[]
    {
        createEntry(true, DEFAULT_PREFIX, DEFAULT_ROLES), createEntry(false, DEFAULT_PREFIX, DEFAULT_ROLES),
        createEntry(true, "", DEFAULT_ROLES), createEntry(false, "", DEFAULT_ROLES),
        createEntry(false, "ActiveMQ.Advisory", DEFAULT_ROLES), createEntry(true, "USERS.", DEFAULT_ROLES),
        createEntry(false, "USERS.", DEFAULT_ROLES)
    }))));
    br.setPlugins(new BrokerPlugin[]
    {
        sap, ap
    });
    return br;
  }

  private AuthorizationEntry createEntry(boolean isQueue, String prefix, String roles) throws Exception {
    AuthorizationEntry entry = new AuthorizationEntry();
    if (isQueue) {
      entry.setQueue(prefix + ">");
    }
    else {
      entry.setTopic(prefix + ">");
    }
    entry.setRead(roles);
    entry.setAdmin(roles);
    entry.setWrite(roles);
    return entry;
  }
}
