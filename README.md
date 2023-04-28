# Interlok

[![GitHub tag](https://img.shields.io/github/tag/adaptris/interlok.svg)](https://github.com/adaptris/interlok/tags)
[![license](https://img.shields.io/github/license/adaptris/interlok.svg)](https://github.com/adaptris/interlok/blob/develop/LICENSE)
[![Actions Status](https://github.com/adaptris/interlok/actions/workflows/gradle-publish.yml/badge.svg)](https://github.com/adaptris/interlok/actions)
[![codecov](https://codecov.io/gh/adaptris/interlok/branch/develop/graph/badge.svg)](https://codecov.io/gh/adaptris/interlok)
[![CodeQL](https://github.com/adaptris/interlok/workflows/CodeQL/badge.svg)](https://github.com/adaptris/interlok/security/code-scanning)
[![Known Vulnerabilities](https://snyk.io/test/github/adaptris/interlok/badge.svg?targetFile=build.gradle)](https://snyk.io/test/github/adaptris/interlok?targetFile=build.gradle)
[![Closed PRs](https://img.shields.io/github/issues-pr-closed/adaptris/interlok)](https://github.com/adaptris/interlok/pulls?q=is%3Apr+is%3Aclosed)
[![Appveyor](https://ci.appveyor.com/api/projects/status/efwdjhhdh5ug3rkk/branch/develop?svg=true)](https://ci.appveyor.com/project/adaptris/interlok/branch/develop)

This is the base repository for Interlok. It contains the base adapter pared of all 3rd party dependencies other than open source ones. There is a single dependency (commented out) on Microsoft SQL Server JDBC provider which is not required unless you explicitly want to test JDBC against SQL Server.

## Setting up your Development Environment ##

### Pre-requisites ###

* JAVA 11

### Optional pre-reqs ###

* MySQL (for tests)
* SFTP Server (for tests)

## Doing your first build ##


```
#!shell

./gradlew compileJava
# You will see if you have setup your environment properly.
./gradlew test
# This will take about 10 minutes, and at the end there will be a directory that contains the test output according to the usual gradle conventions (build/reports/tests/test)
```
### Fixing your build ###

Your first build may well have errors!

* Have you installed the (JCE) Unlimited Strength Jurisdiction Policy Files files? (some of the unit tests rely on strong crypto...); If you're using an OpenJDK build, then I think it's automatically installed unless you have explicitly disabled it.
* Windows User? then you will have to *at least once* encrypt a file so that an EFS certificate is created (for com.adaptris.security.password)

### JDK work-arounds ###

* Windows User? With a 32bit JVM you might get some stupid __Could not reserve enough space for object heap/OutOfMemory__; just stop it, and use a 64 bit one.

## Modifying your build ##

Everything relies on build.properties; in the previous section you didn't make any changes, so it skipped a bunch of tests that required the optional components. You might want to enable those tests, possibly because you're working on fixing those components or well whatever. So what you will need to do is to create your own copy of build.properties which is simply a standard "properties" file with key=value entries.

### Standard properties ###

Define your properties either on the gradle commandline `gradle -PpropertyName=value test` etc. or in gradle.properties

Property Key | Default Value | Description | Notes
------------ | ------------- | ----------- | -----
junit.test.classes|```**/*Test*```|Standard filter so that when you run _gradle test_ it only tests what you want to test ||
verboseTests|false|If your console mode is "plain" (org.gradle.console=plain), then this prints out each test suite before it's executed|WinGit : it is probably plain|

So, `gradle -PverboseTests=true test` will print out test suite names; gradle `-Pjunit.test.classes=**/metadata/**/*Test* test` will just test metadata classes.

### Custom Tests

Create a file `src/test/resources/default-test.properties.template.<machinename>`. This will be imported as part of the `processTestResources` step and will allow you control behaviour within individual tests such as whether or not stored procedure tests are run (or actual SFTP against a real server).

Check default-test.properties.template for specific values.

## How to publish your changes ##

If you've made some changes, and you want to check that the downstream projects (like interlok-optional) are OK with your changes, then the easiest solution is to build the snapshot release from Jenkins. This will trigger all the downstream builds once successful (it might take a while) and you'll be able to see any impact your changes may have had.


And that's it.
