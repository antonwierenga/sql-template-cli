organization := "com.antonwierenga"

name := "sql-template-cli"

version := "0.1.0"

scalaVersion := "2.12.1"

licenses += ("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

libraryDependencies += "org.springframework.shell" % "spring-shell" % "1.2.0.RELEASE"
libraryDependencies += "com.typesafe" % "config" % "1.3.1"
libraryDependencies += "org.scala-lang" % "jline" % "2.11.0-M3"
libraryDependencies += "junit" % "junit" % "4.8" % "test"
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test->default"
libraryDependencies += "org.scala-lang.modules" % "scala-xml_2.12" % "1.0.6"
libraryDependencies += "log4j" % "log4j" % "1.2.17"
libraryDependencies += "com.h2database" % "h2" % "1.4.194"
libraryDependencies += "org.apache.poi" % "poi" % "3.15"

import scalariform.formatter.preferences._

scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(RewriteArrowSymbols, true)

import de.heikoseeberger.sbtheader.license.Apache2_0

headers := Map(
    "scala" -> Apache2_0("2017", "Anton Wierenga"),
    "conf" -> Apache2_0("2017", "Anton Wierenga", "#")
)

enablePlugins(AutomateHeaderPlugin) 
enablePlugins(JavaAppPackaging)

scriptClasspath := Seq("*")

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

scalacOptions += "-target:jvm-1.7"

parallelExecution in Test := false

javaOptions in Universal ++= Seq("--disableInternalCommands")
