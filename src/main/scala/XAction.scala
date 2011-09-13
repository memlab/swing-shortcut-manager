package edu.upenn.psych.memory.shortcutmanager

case class XAction(val className: String,
                   name: String,
                   tooltip: Option[String],
                   shortcut: Option[Shortcut])
