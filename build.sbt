//BASIC PROJECT INFO
name := "swing-shortcut-manager"

organization := "edu.upenn.psych.memory"

version := "0.2.0"

//SCALA VERSIONS AND OPTIONS
scalaVersion := "2.9.1"

scalacOptions ++= Seq("-deprecation", "-unchecked")

javacOptions ++= Seq("-Xlint:unchecked")

//ENTRY POINT
mainClass in (Compile, packageBin) :=
  Some("edu.upenn.psych.memory.shortcutmanager.Main")

mainClass in (Compile, run) :=
  Some("edu.upenn.psych.memory.shortcutmanager.Main")

//JAVA DEPENDENCIES
libraryDependencies ++= Seq (
  "jdom" % "jdom" % "1.1",
  "jaxen" % "jaxen" % "1.1.1"
)

//SBT BEHAVIOR
fork in Compile := true

logLevel := Level.Info //higher than Info suppresses your own printlns

traceLevel := 5

//PROGUARD
seq(ProguardPlugin.proguardSettings :_*)

makeInJarFilter <<= (makeInJarFilter) {
  (makeInJarFilter) => {
    (file) => file match {
      case "icu4j-2.6.1.jar" => makeInJarFilter(file) +
        ",!com/ibm/icu/impl/data/LocaleElements_zh__PINYIN.class"
      case "xml-apis-1.3.02.jar" => makeInJarFilter(file) + ",!**"
      case _ => makeInJarFilter(file)
    }
  }
}

proguardOptions ++= Seq (
  "-dontshrink -dontoptimize -dontobfuscate -dontpreverify -dontnote " +
  "-ignorewarnings",
  keepAllScala
)
