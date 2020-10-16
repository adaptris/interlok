---
layout: page
title: 0008-restful-failed-message-retrier
---
# Failed Message Retrier should have a REST endpoint

* Status: ACCEPTED
* Deciders: Lewin Chan, Matt Warman, Aaron McGrath, Sebastien Belin, Paul Higginson
* Date: 2020-10-13

## Context and Problem Statement

If you are deployed somewhere that is an ephemeral filesystem then we can't rely on standard error handlers/retriers failed messages in  (unless you use a docker volume mount or similar)l; so we need to have story or some codified best practise such that people can use it easily when they are deployed into docker containers and the like.

If you are full ESB stylings, and you have dead-letter queues, then perhaps this doesn't matter, since everything would work off a queue, and be triggered by message from a queue.

## Decision Drivers

* Minimal configuration required to get the desired behaviour
* Upgrade to behaviour should not require changes to multiple interlok configurations.
* Filesystem agnostic
* Think about how this might manifest itself as a 'Work-Unit'

## Considered Options

* Workflow Based (do nothing)
* Pluggable Storage (LocalFS/S3 initially moving to jclouds parity)

## Decision Outcome

Jetty Trigger; however, there is documentation that needs to happen around the behaviour of RetryMessageErrorHandler vis-a-vis JMS Async producers.


### Vanilla workflow based system.

We take as our base implementation from [this blog post](https://interlok.adaptris.net/blog/2017/10/19/interlok-s3-error-store.html). The configuration detailed therein could be made into a template, and subsequently imported into multiple interlok instances; but maintaining the configuration is harder than it needs to be vis-a-vis upgrading, managing multiple instances that want to use this concept.

- Good, because no changes
- Bad, hard to maintain

### Jetty Trigger

Still taking [this blog post](https://interlok.adaptris.net/blog/2017/10/19/interlok-s3-error-store.html) as your feature set but with the design decision to separate out the raw payload away from the metadata. This is simply a pragmatic choice so that if we are dealing with arbitrarily large messages we don't need
to think about how to encode both the payload + metadata into a single blob.

The target is to be able to something like this :
- `curl -XGET http://localhost/api/list-failed` and get a JSON array back that essentially that does an _ls of the target directory_
- `curl -XPOST http://localhost:8080/api/retry/{msg-id}` and get that message automatically retried to the target workflow.

This can be described with a pretty diagram generated [via plantuml](./assets/0008-restful-sequence.puml)

![Sequence](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/adaptris/interlok/develop/docs/adr/assets/0008-restful-sequence.puml)


This boils down to a new FailedMessageRetrier implementation (`RetryFromJetty`) and a supporting interface in `interlok-core`. Since FailedMessageRetriers know about all the workflows, this is the right object to use. We can re-use whatever components we need to.

```java
public class RetryFromJetty implements FailedMessageRetrier {
  // When configuring the JettyConsumer
  // Same JettyConsumer for Reporting ?
  // /api/retry -> /api/retry/*
  // /api/retry/ -> /api/retry/*
  // /api/retry/* -> /api/retry/*
  private String retryEndpointPrefix;
  private String reportingEndpoint;
  private JettyConnection connection;
  private BlobListRenderer reportRenderer;
  private RetryHandler handler;
  @AdvancedConfig(rare=true)
  private String retryHttpMethod="POST"
  @AdvancedConfig
  private Boolean deleteAfterSubmit;
}
```

```java
public interface RetryHandler {
  List<RemoteFile> report()
  AdaptrisMessage buildForRetrying();
  void delete(String msgId);
}
```

This ultimately manifests itself with XML two separate elements

```xml
 <failed-message-retrier class="retry-from-jetty">
   <retry-endpoint-prefix>/api/retry/</retry-endpoint-prefix>
   <reporting-endpoint>/api/list-failed</reporting-endpoint>
   <jetty-connection class="embedded-jetty-connection"/>
   <report-renderer class="json-blob-list-renderer"/>
   <handler class="from-fs">
    <base-url>file://localhost/./fs/errors</base-url>
    <payload-name>payload.bin</payload-name>
    <metadata-name>metadata.properties</metadata-name>
   </handler>
 </failed-message-retrier>
```

And a service that understands what it needs to write to the filesystem (rather than a StandaloneProducer + FsProducer).

```xml
<service class="put-message-on-filesystem">
  <bucket-name>bucket</bucket-name>
  <base-url>ile://localhost/./fs/errors</base-url>
  <payload-name>payload.bin</payload-name>
  <metadata-name>metadata.properties</metadata-name>
  <!-- Consider having 2 "locational" pieces of metadata that can used by
       later services.
    failed-message-payload=file://localhost/./fs/errors/<msg-id>/payload.bin
    failed-message-metadata=file://localhost/./fs/errors/<msg-id>/metadata.properties
  -->
</service>
```

- Good, because pluggable
- Good, because it abstracts the behavioural complexity
- Bad, it's probably not as free form as some power-users want

#### Switching to cloud storage.

If we consider the extended use-case which is to use S3 / Azure as your blob storage then this would have 2 custom implementations (for explicitness)

```xml
   <handler class="get-from-s3">
    <aws-connection class="timebombed-credentials"/>
    <bucket-name>bucket</bucket-name>
    <s3-prefix>MyInterlokInstance/failed/</s3-prefix>
    <payload-name>payload.bin</payload-name>
    <metadata-name>metadata.properties</metadata-name>
   </handler>
```

And something that "does the same thing" as part of a message-error-handler chain.

```xml
<service class="upload-message-to-s3">
  <aws-connection class="timebombed-credentials"/>
  <bucket-name>bucket</bucket-name>
  <s3-prefix>MyInterlokInstance/failed/</s3-prefix>
  <payload-name>payload.bin</payload-name>
  <metadata-name>metadata.properties</metadata-name>
  <!-- Consider having 2 "locational" pieces of metadata that can used by
       later services.
    failed-message-payload=s3://bucket/path/to/<msg-id>/payload.bin
    failed-message-metadata=s3://bucket/path/to/<msg-id>/metadata.properties
  -->
</service>
```
