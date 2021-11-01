enablePlugins(CalibanPlugin)

resolvers += "Sonatype OSS Snapshots s01" at "https://s01.oss.sonatype.org/content/repositories/snapshots"

scalacOptions += "-language:experimental.fewerBraces"

scalaVersion := "3.1.1-RC1-bin-20210927-3f978b3-NIGHTLY"
//scalaVersion := "3.1.0"

libraryDependencies ++= Seq(
  "com.github.ghostdogpr" %% "caliban" % "1.2.1",
  "com.github.ghostdogpr" %% "caliban-zio-http" % "1.2.1",
  "com.github.ghostdogpr" %% "caliban-client" % "1.2.1",
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % "3.3.16",

  "org.slf4j" % "slf4j-simple" % "1.7.30" % Runtime,
)

/*
Compile / caliban / calibanSettings += calibanSetting(url("http://localhost:8088/api/graphql"))(
  cs =>
    cs.clientName("ExampleServiceClient")
      .packageName("com.example.graphql.client")
)
 */

/*
Compile / caliban / calibanSettings += calibanSetting(file("My.graphql")) {
  _.packageName("com.example.graphql.client")
}
 */