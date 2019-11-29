import ReleaseTransformations._

lazy val finchVersion = "0.31.0"

lazy val buildSettings = Seq(
  organization := "com.github.finagle",
  scalaVersion := "2.12.7",
  crossScalaVersions := Seq("2.11.12", "2.12.7")
)

lazy val finagleOAuth2Version = "19.8.0"
lazy val circeVersion = "0.11.1"

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
  "org.mockito" % "mockito-all" % "1.10.19",
  "org.scalacheck" %% "scalacheck" % "1.14.1",
  "org.scalatest" %% "scalatest" % "3.1.0"
)

val baseSettings = Seq(
  libraryDependencies ++= Seq(
    "com.github.finagle" %% "finchx-core" % finchVersion,
    "com.github.finagle" %% "finagle-oauth2" % finagleOAuth2Version
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
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  pgpSecretRing := file("local.secring.gpg"),
  pgpPublicRing := file("local.pubring.gpg"),
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  homepage := Some(url("https://github.com/finagle/finch")),
  autoAPIMappings := true,
  apiURL := Some(url("https://finagle.github.io/finch/docs/")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/finch/finch-oauth2"),
      "scm:git:git@github.com:finch/finch-oauth2.git"
    )
  ),
  releaseVersionBump := sbtrelease.Version.Bump.Minor,
  releaseProcess := {
    Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      releaseStepCommandAndRemaining("+clean"),
      releaseStepCommandAndRemaining("+test"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publishSigned"),
      setNextVersion,
      commitNextVersion,
      releaseStepCommand("sonatypeReleaseAll"),
      pushChanges
    )
  },
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
      <developer>
        <id>ImLiar</id>
        <name>Sergey Kolbasov</name>
        <url>https://twitter.com/sergey_kolbasov</url>
      </developer>
    </developers>
)

lazy val allSettings = baseSettings ++ buildSettings ++ publishSettings

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val root = project.in(file("."))
  .settings(allSettings)
  .settings(noPublish)
  .aggregate(oauth2, examples)

lazy val oauth2 = project
  .settings(moduleName := "finchx-oauth2")
  .settings(allSettings)

lazy val examples = project
  .settings(moduleName := "finchx-oauth2-examples")
  .settings(allSettings)
  .settings(noPublish)
  .settings(resolvers += "TM" at "http://maven.twttr.com")
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-generic" % circeVersion,
      "com.github.finagle" %% "finch-circe" % finchVersion
    )
  )
  .dependsOn(oauth2)
