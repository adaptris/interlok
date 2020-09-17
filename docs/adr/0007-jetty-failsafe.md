---
layout: page
title: 0007-jetty-failsafe
---
# Allow jetty management component to run w/o any configuration

* Status: Accepted
* Deciders: Aaron McGrath, Lewin Chan
* Date: 2020-05-13

## Context and Problem Statement

There are 2 ways to enable the jetty management component in bootstrap.properties. The first bootstraps jetty using the configured XML configuration file, the second bootstraps a jetty server with minimal defaults; it also doesn't by default deploy the webapp provider which means that the UI is not started (unless the optional `webServerWebappUrl` is specified). We have always considered the `FromProperties` variant to be not production ready and a message is logged to that effect on startup.
```
managementComponents=jmx:jetty
webServerConfigUrl=./config/jetty.xml
## if you want to override the jetty port for instance, you can do that.
sysprop.jetty.http.port=18080
```

```
managementComponents=jmx:jetty
webServerPort=8080
webServerWebappUrl=./webapps
```

Normally, as well, the jetty.xml contains a reference to the default descriptors file

```xml
<Set name="defaultsDescriptor">
  <Property>
    <Name>jetty.deploy.defaultsDescriptorPath</Name>
    <Default>
      <Property name="jetty.home" default="." />/config/webdefault.xml
    </Default>
  </Property>
</Set>
```

Generally most people don't edit the jetty.xml from the default, which means that for all gradle style projects we need to carry around a `src/main/interlok/config/jetty.xml + webdefault.xml` inside our git configuration even if we don't use it formally within our Interlok configuration

So the problem is that we need to be able to start up the jetty management component with actual sensible defaults if the jetty component is enabled, but no port / config file is specified.


## Considered Options

* Do nothing
* Make the `FromProperties` _production ready_
* Embed the jetty.xml into interlok-common.jar and use it if jetty is enabled but no other settings are defined.

## Decision Outcome

Embed the jetty.xml into interlok-common.jar and use it if jetty is enabled but no other settings are defined.

## Pros and Cons of the Options

### Do Nothing

This is of course the easy option. There is no change for the user, and since there are now the `template-*` files that the UI can emit as part of its project saving, this might not make any difference in the medium term, since most projects will automatically get a jetty.xml + webdefault.xml present if they save the project with the "generate my bootstrap files" option in the UI.

* Good, because no change to code.
* Bad, additional artefacts cluttering interlok projects (additional objects in git)
* Neutral, if we build a new deployment from scratch from the UI, this problem doesn't manifest itself in 3.10.1 onwards

### Make `FromProperties` production ready

This tightly couples our default configuration with a jetty version; effectively we would have to replicate a variant of the existing jetty.xml in code directly using the jetty setters and getters. This adds no benefit to us, but adds additional maintenance burden. If we want to expose various things as configuration for the user we would have to define properties for inclusion into `bootstrap.properties` or define another fine to load them from which puts us back into the same class of problem of _always having to have a jetty.xml file_.

* Bad, tight coupling to internal jetty objects
* Bad, if we wanted configurability we would have to expose them all as webserver.XXX properties or similar.
* Bad, maintenance overhead

### Embed jetty.xml

Since we already have a `jetty-webdefault-failsafe.xml` present in interlok-common.jar we can have a `jetty-failsafe.xml` as well. We then introduce a new ServerBuilder implementation that can build a jetty configuration from an XML file(s) found on the classpath.

* Add a new `FromClasspath` ServerBuilder implementation (extracting some commonality out of `FromXmlConfig`)
* Only takes effect if `webServerConfigUrl` and `webServerPort` are not defined.
* `FromClasspath` uses _jetty-failsafe.xml_ and _jetty-webdefault-failsafe_ to build the jetty instance.

Since the java.net.URL can be used to reference files inside jars, this already works (i.e. there is a jetty.xml inside a custom jar `zzlc-jetty.jar`) we can find the resource, and create a URL from it before calling Resource.newResource(url);

```
sysprop.jetty.deploy.defaultsDescriptorPath=jar:file:/C:/adaptris/work/runtime/gradle-nightly/interlok-3219-jetty-classpath/build/distribution/lib/zzlc-jetty.jar!/META-INF/webdefault.xml
webServerConfigUrl=jar:file:/C:/adaptris/work/runtime/gradle-nightly/interlok-3219-jetty-classpath/build/distribution/lib/zzlc-jetty.jar!/META-INF/jetty.xml
```
Basically jetty.xml remains the same, but we overload the jetty.deploy.defaultsDescriptorPath with the location of `jetty-webdefault-failsafe.xml` in interlok-common.jar

* Good, jetty starts with a sensible set of defaults
* Neutral, still a maintenance overhead, but on a single file, since we already have to check that the default jetty.xml works anyway.
* Neutral, if we build a new deployment from scratch from the UI, this problem doesn't manifest itself in 3.10.1 onwards
