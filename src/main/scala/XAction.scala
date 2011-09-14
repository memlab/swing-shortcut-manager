package edu.upenn.psych.memory.shortcutmanager

case class XAction(val className: String,
                   enum: Option[String],
                   name: String,
                   tooltip: Option[String],
                   shortcut: Option[Shortcut]) {

  val javaTooltip = tooltip.getOrElse(null)
  val javaShortcut = shortcut.getOrElse(null)
  val javaEnum = enum.getOrElse(null)

  val id = {
    enum match {
      case Some(str) => List(className, str).mkString("-")
      case None => className
    }
  }
}
