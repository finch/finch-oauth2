
[![Build Status](https://travis-ci.com/finch/finch-oauth2.svg?branch=master)](https://travis-ci.com/finch/finch-oauth2)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.finagle/finch-oauth2_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.finagle/finch-oauth2_2.12)

----

This project provides OAuth2 support backed by the [finagle-oauth2][finagle-oauth2] library.

The project is published in Maven Central. Adding it to sbt is as follows:
```scala
libraryDependencies ++= Seq(
  "com.github.finagle" %% "finch-oauth2" % "version"
)
```

There are usage examples in [finch documentation](finch-oauth-cookbook).

[finagle-oauth2]: https://github.com/finagle/finagle-oauth2
[finch-oauth-cookbook]: http://finagle.github.io/finch/cookbook.html#oauth2
