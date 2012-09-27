/* basic project info */
name := "swing-shortcut-manager"

organization := "com.yuvimasory"

version := "1.0.0"

/* scala versions and options */
scalaVersion := "2.9.2"

crossScalaVersions := Seq(
  "2.9.1-1", "2.9.1", "2.9.0-1", "2.9.0"
)

scalacOptions ++= Seq("-deprecation", "-unchecked")

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

/* java dependencies */
libraryDependencies ++= Seq (
  "jdom" % "jdom" % "1.1",
  "jaxen" % "jaxen" % "1.1.1"
)

/* entry point */
mainClass in (Compile, run) :=
  Some("edu.upenn.psych.memory.shortcutmanager.Main")

/* sbt behavior */
fork in Compile := true

logLevel := Level.Info //higher than Info suppresses your own printlns

traceLevel := 5

/* publishing */
publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some(
    "snapshots" at nexus + "content/repositories/snapshots"
  )
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/memlab/swing-shortcut-manager</url>
  <licenses>
    <license>
      <name>GPLv3</name>
      <url>http://www.gnu.org/licenses/gpl-3.0.html</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>https://github.com/memlab/swing-shortcut-manager.git</url>
    <connection>scm:git:https://github.com/memlab/swing-shortcut-manager.git</connection>
  </scm>
  <developers>
    <developer>
      <id>ymasory</id>
      <name>Yuvi Masory</name>
      <email>ymasory@gmail.com</email>
      <url>http://yuvimasory.com</url>
    </developer>
  </developers>
)

