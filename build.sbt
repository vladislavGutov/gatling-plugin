import sbt.Keys.organization

lazy val commonSettings = Seq(
  organization := "com.github.vladislavGutov",
  scalaVersion := "2.12.4",
  version := "1.0.0"
) ++ assemblySettings

lazy val assemblySettings = Seq(
  assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
)

lazy val commonDependencies = {
  libraryDependencies ++= Seq(
    "io.gatling" % "gatling-core" % "2.3.0" % Provided,
    "com.typesafe.akka" %% "akka-actor" % "2.5.4" % Provided
  )
}

lazy val gatlingPlugin = (project in file("gatling-plugin"))
  .settings(commonSettings)
  .settings(commonDependencies)
  .settings(
    name := "gatling-plugin"
  )
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.kafka" % "kafka-clients" % "0.11.0.2" excludeAll ExclusionRule("org.slf4j", "slf4j-api"),
      "javax.jms" % "javax.jms-api" % "2.0",
      "org.json4s" %% "json4s-jackson" % "3.5.3",
      "org.mongodb.scala" %% "mongo-scala-driver" % "2.2.1"
    )
  )

lazy val testRunner = (project in file("runner"))
  .settings(commonSettings)
  .settings(commonDependencies)
  .settings(
    libraryDependencies := libraryDependencies.value.map {
      module =>
        if (module.configurations.equals(Some("provided"))) {
          module.copy(configurations = None)
        } else {
          module
        }
    }
  )
  .settings(
    libraryDependencies ++= Seq(
      "io.gatling" % "gatling-app" % "2.3.0",
      "io.gatling.highcharts" % "gatling-highcharts" % "2.3.0",
      "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.3.0"
    )
  )
  .dependsOn(gatlingPlugin)