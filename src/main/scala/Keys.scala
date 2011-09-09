package edu.upenn.psych.memory.keyboardmanager

import javax.swing.KeyStroke

import scala.util.Properties


case class Shortcut(stroke: KeyStroke) {

  val sysSep   = if (Properties.isMac) ""  else "+"
  val sysAlt   = if (Properties.isMac) "⌥" else "Alt"
  val sysCtrl  = if (Properties.isMac) "^" else "Ctrl"
  val sysMeta  = if (Properties.isMac) "⌘" else "Meta"
  val sysShift = if (Properties.isMac) "⇧" else "Shift"

  override val toString = {
    val parts = serialized.split(Shortcut.SerializationDelimiter).toList 
    val newParts: List[String] =
      parts map { el =>
        el match {
          case "alt"   => sysAlt
          case "ctrl"  => sysCtrl
          case "meta"  => sysMeta
          case "shift" => sysShift
          case _       => el
        }
      }
    newParts mkString sysSep
  }

  lazy val serialized: String = {
    val parts =
      KeyUtils.serialize(stroke).split(
        Shortcut.SerializationDelimiter).toList
    val newParts: List[String] =
      parts flatMap { el =>
        el match {
          case "pressed" => None
          case _         => Some(el)
        }
      }
    newParts mkString Shortcut.SerializationDelimiter
  }
}

object Shortcut {
  
  val SerializationDelimiter = " "
  val MetaName = "meta"
  val CtrlName = "ctrl"

  def parse(str: String): Shortcut = {
    Shortcut(KeyStroke.getKeyStroke(str))
  }.ensuring(_ != null)

  def normXmlKey(str: String) = {
    str match {
      case "menu"    => if (Properties.isMac) MetaName else CtrlName
      case "command" => MetaName
      case _         => str
    }
  }
}
