package edu.upenn.psych.memory.shortcutmanager

case class XAction(val className: String,
                   arg: Option[String],
                   name: String,
                   tooltip: Option[String],
                   shortcut: Option[Shortcut]) {
  val id = List(className, arg.getOrElse("")).mkString("-")
}
