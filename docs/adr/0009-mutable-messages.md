---
layout: page
title: 0009-mutable-messages
---
# Mutable message

* Status: DRAFT
* Deciders: Lewin Chan, Matt Warman, Aaron McGrath, Sebastien Belin
* Date: 2020-10-08

## Context and Problem Statement

There is a specific use-case where Interlok may have possibly been the wrong choice (gasp!) and it is essentially doing this

![Sequence](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/adaptris/interlok/ADR-0009-mutable-messages/docs/adr/assets/0009-mutable-messages-usecase.puml)

It's essentially behaving as a passthrough stream of data into Elasticsearch. It's arguable that this is the wrong approach but ![Interlok Hammer](https://img.shields.io/badge/certified-interlok%20hammer-red.svg)

- There is an IOPS cost for Interlok to download and then subsequently stream it into Elasticsearch; we aren't doing any thing with the payload.

What are the options that we can provide to mitigate the problem.

## Considered Options

- Do Nothing
- Custom JSON parsing message factory
- Mutable Message
- Streaming Workflow

## Decision Outcome

...

### Do Nothing

Behaviour remains the same, they can probably scale the EC2 instance so that it has more performance in situations like this.
Since it's primarily an asynchronous process; then it's perfectly reasonable to offload this to a messaging backbone for horizontal scalability.

#### Good/bad considerations.

* Good, because work is hard.

### JSON + S3 aware AdaptrisMessageFactory

Since this is a specialised use case we can effectively write our own splitter instance such that the message factory knows how to connect to S3, and have a read only inputStream that streams directly from the file.

Since the large-json-array-splitter effectively does this (in code)

```java
protected AdaptrisMessage constructAdaptrisMessage() throws IOException {
  AdaptrisMessage tmpMessage = newMessage();
  if (parser.nextToken() == JsonToken.START_OBJECT) {
    ObjectNode node = mapper.readTree(parser);
    tmpMessage.setStringPayload(node.toString());
    return addMetadata(tmpMessage);
  }
  return null;
}
```

Then we can have additional behaviour the "first time" we call setStringPayload()/setContent() around parsing the JSON payload and then attempting to wrap the S3 blob effectively replace the _N>1 JSON path operations_ and _Get JSON Blob_ steps inside the message factory.

Our caveats would have to be
- getInputStream / getReader() can be called multiple times, and each call will open a direct inputstream to the S3 object.
- getOutputStream / getWriter() always throws an exception (so we don't replace the S3 blob)
  - Or it's allowed once and once only.
- setStringPayload/setContent is allowed once and once only.
- If you use `%payload{jsonpath:$.1.2.3}` is still possible but has a huge performance hit every every time (might just throw an UnsupportedOperationException here)

How this manifests itself is something like this :

```xml
<services>
  <advanced-message-splitter-service>
    <unique-id>iterate-over-list-files-results</unique-id>
    <splitter class=”json-array-splitter”>
      <message-factory class=”json-readonly-s3-message-factory”>
        <s3-connection class=”aws-s3-connection”/>
        <path-to-s3-bucket>$.bucketName</path-to-s3-bucket>
        <path-to-prefix>$.prefix</path-to-prefix>
        <path-to-name>$.name</path-to-name>
      </message-factory>
    </splitter>
    <service class="service-list">
      <services>
        <standalone-producer>
          <producer class="elastic-rest-bulk-operation"/>
        </standalone-producer>
      </services>
    </service>
  </advanced-message-splitter>
</services>
```

#### Good/bad considerations.

- Good, we never "download the file" until we need to.
- Bad, because we have to guarantee that nothing ever touches the payload
- Bad, If you use `%payload{jsonpath:$.1.2.3}` is still possible but has a huge performance hit every every time (might just throw an UnsupportedOperationException here)
- Unknown, Does S3 give us an InputStream or is it the case that the DownloadManager doesn't expose that to us.
- Unknown, We use TransferManager which multiplexes the download (to make it faster) -> if we're reading directly from the InputStream does this even help?
- Unknown, Needs prototyping and testing for speed...

### Mutable Message / Mutable Message Factory.

AdaptrisMessage beheaviour isn't mutable at the moment; it's either FileBacked / ZipFileBacked or all in memory. You make that decision when you configure the consumer.

What if we had effectively an AdaptrisMessage implementation that simply proxies other AdaptrisMessage instances. This means we can change the underlying type without impacting behaviour that much.

What does this mean in terms of configuration. I see it as something like this.

```xml
<services>
  <advanced-message-splitter-service>
    <unique-id>iterate-over-list-files-results</unique-id>
    <splitter class="json-array-splitter">
       <message-factory class="mutable-message-factory">
         <base-factory class="default-message-factory"/>
       </message-factory>
    </splitter>
    <service class="service-list">
      <services>
        <json-path-service selecting the stuffs/>
        <mutate-message>
          <message-factory class="s3-read-only-backed-message-factory">
            <connection class="s3-aws-connection"/>
            <bucket>%message{bucket}</bucket>
            <object-name>%message{id}</object-name>
          </message-factory>
        </mutate-message>
        <standalone-producer>
          <producer class="elastic-rest-bulk-operation"/>
        </standalone-producer>
      </services>
    </service>
  </advanced-message-splitter-service>
</services>
```

For the use-case in question it's effectively a few new classes.
- a new `mutable-message-factory` that creates a proxy message instance.
   - a new `mutable-message` that does the proxying.
- a new `mutate-message` service that allows you to switch the underlying message implementation.
- a new `s3-readonly-message-factory` that knows to build an inputstream from s3://
   - a new s3-readonly-message.

Since it's a readonly message; it still has the same caveats as mentioned above.

#### Good/bad considerations.

- Good, we never "download the file" until we need to.
- Good, because this isn't "tied" to the specific use-case.
- Bad, because we have to guarantee that nothing ever touches the payload
- Bad, If you use `%payload{jsonpath:$.1.2.3}` is still possible but has a huge performance hit every every time (might just throw an UnsupportedOperationException here)
- Bad, The internal shortcuts that know about FileBackedMessageFactory would be broken because `instanceOf` is no longer valid.
- Unknown, Does S3 give us an InputStream or is it the case that the DownloadManager doesn't expose that to us.
- Unknown, We use TransferManager which multiplexes the download (to make it faster) -> if we're reading directly from the InputStream does this even help?
- Unknown, Needs prototyping and testing for speed...


## Streaming Workflow

- Or Streaming Service... TBC

