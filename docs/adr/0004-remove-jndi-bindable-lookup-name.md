---
layout: page
title: 0004-remove-jndi-bindable-lookup-name
---
# Deprecate and ultimately remove JndiBindable#getLookupName()

* Status: Accepted
* Deciders: Aaron McGrath, Lewin Chan, Matt Warman, Paul Higginson, (Sebastien Belin)
* Date: 2019-07-18


## Context and Problem Statement

As recorded in [INTERLOK-2872](https://adaptris.atlassian.net/browse/INTERLOK-2872) lookup-name as a field has little or no meaning within the UI; in fact, it may not be entirely supported fully within the UI.

The background of the `lookup-name` is so that you can override the name+prefix for the shared component that is stored within the internal JNDI server. 

If you have

```
<ftp-connection>
  <unique-id>MyFtpConnection</unique-id>
</ftp-connection>
```
This would be stored in JNDI under the name of `comp/env/MyFtpConnection`; if you need to reference this in configuration then you would just use `lookup-name=MyFtpConnection` in your _SharedConnection_

If you have

```
<ftp-connection>
  <unique-id>MyFtpConnection</unique-id>
  <lookup-name>comp/env/ftp/MyFtpConnection</lookup-name>
</ftp-connection>
```

This would be stored in JNDI under the name of `comp/env/ftp/MyFtpConnection`; if you need to reference this in configuration then you would just use `lookup-name=comp/env/ftp/MyFtpConnection` in your _SharedConnection_; this is possibly the thing that might never have worked in the UI.

There is a special case for `DatabaseConnection` subclasses; the _javax.sql.DataSource_ is bound against `comp/env/jdbc/unique_id`. Which means that you can address the datasource within _persistence.xml_ as `adapter:comp/env/jdbc/unique_id`. If lookup-name is specified then this is used to bind the _javax.sql.DataSource_ (__which might actually mean you can't ever get to the DatabaseConnection again via a SharedConnection__)

## Considered Options

* Do Nothing
* Deprecate and remove in 3.11.0

## Decision Outcome

Chosen option: Deprecate and remove in 3.11.0

### Do Nothing

If the user already has the extended documentation available; it already mentions.
We would further hide the field using the new annotation that becomes available as part of [INTERLOK-2663](https://adaptris.atlassian.net/browse/INTERLOK-2663).

* Good, because we don't have to do much work.
* Bad, because we might have to make the UI support this style of configuration.
* Bad, because DatabaseConnection will still have odd behaviour if you specify the lookup-name
* Bad, because it's ambiguous in the context of configuration; regardless of how much documentation we write no-one ever reads it.

### Deprecate

We mark the field as deprecated in 3.9.1; with an intention to remove in 3.11.0. 

* Good, because it fixes a logical flaw with DatabaseConnection when someone actually specifies lookup-name
* Good, because the UI doesn't have to change.
* Neutral, because there hasn't been a production instance where someone has used lookup-name!
* Bad, because we're going to remove methods from an interface...

