//BASIC PROJECT INFO
name := "keyboard-manager"

organization := "edu.upenn.psych.memory"

version := "prototype"

//SCALA VERSIONS AND OPTIONS
scalaVersion := "2.9.1"

scalacOptions ++= Seq("-deprecation", "-unchecked")

javacOptions ++= Seq("-Xlint:unchecked")

//ENTRY POINT
mainClass in (Compile, packageBin) :=
  Some("edu.upenn.psych.memory.keyboardmanager.Main")

mainClass in (Compile, run) := Some("edu.upenn.psych.memory.keyboardmanager.Main")

//SBT BEHAVIOR
fork in Compile := true

logLevel := Level.Info //higher than Info suppresses your own printlns

traceLevel := 5

//PROGUARD
seq(ProguardPlugin.proguardSettings :_*)

proguardOptions ++= Seq (
    "-dontshrink -dontoptimize -dontobfuscate -dontpreverify -dontnote " +
    "-ignorewarnings",
    keepAllScala
)
