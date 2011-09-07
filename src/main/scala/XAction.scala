package edu.upenn.psych.memory.keyboardmanager

case class XAction(val className: String,
                   name: String,
                   tooltip: Option[String],
                   shortcut: Option[Shortcut])
