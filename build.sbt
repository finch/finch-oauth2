lazy val buildSettings = Seq(
  organization := "com.github.finagle",
  version := "0.16.0-SNAPSHOT",
  scalaVersion := "2.12.2",
  crossScalaVersions := Seq("2.11.11", "2.12.2")
)

lazy val finchVersion = "0.14.0"
lazy val finagleOAuth2Version = "0.6.45"

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture",
  "-Xlint"
)

val testDependencies = Seq(
  "org.scalacheck" %% "scalacheck" % "1.13.5",
  "org.scalatest" %% "scalatest" % "3.0.3"
)

val baseSettings = Seq(
  libraryDependencies ++= Seq(
    "com.github.finagle" %% "finch-core" % finchVersion,
    "com.github.finagle" %% "finagle-oauth2" % finagleOAuth2Version,
    "commons-codec" % "commons-codec" % "1.10",
    "org.mockito" % "mockito-all" % "1.10.19" % "test"
  ) ++ testDependencies.map(_ % "test"),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  scalacOptions ++= compilerOptions ++ Seq("-Ywarn-unused-import"),
  scalacOptions in (Compile, console) ~= (_ filterNot (_ == "-Ywarn-unused-import")),
  scalacOptions in (Compile, console) += "-Yrepl-class-based"
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  homepage := Some(url("https://github.com/finagle/finch")),
  autoAPIMappings := true,
  apiURL := Some(url("https://finagle.github.io/finch/docs/")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/finagle/finch"),
      "scm:git:git@github.com:finagle/finch.git"
    )
  ),
  pomExtra :=
    <developers>
      <developer>
        <id>vkostyukov</id>
        <name>Vladimir Kostyukov</name>
        <url>http://vkostyukov.net</url>
      </developer>
      <developer>
        <id>travisbrown</id>
        <name>Travis Brown</name>
        <url>https://meta.plasm.us/</url>
      </developer>
      <developer>
        <id>rpless</id>
        <name>Ryan Plessner</name>
        <url>https://twitter.com/ryan_plessner</url>
      </developer>

    </developers>
)

lazy val allSettings = baseSettings ++ buildSettings ++ publishSettings

lazy val oauth2 = (project in file("."))
  .settings(moduleName := "finch-oauth2")
  .settings(allSettings)