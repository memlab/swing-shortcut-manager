package edu.upenn.psych.memory.keyboardmanager

import javax.swing.KeyStroke

import scala.util.Properties


case class Shortcut(stroke: KeyStroke) {

  val sysSep   = if (Properties.isMac) ""  else "+"

  val sysAlt    = if (Properties.isMac) "⌥" else "Alt"
  val sysCtrl   = if (Properties.isMac) "^" else "Ctrl"
  val sysMeta   = if (Properties.isMac) "⌘" else "Meta"
  val sysShift  = if (Properties.isMac) "⇧" else "Shift"
  val sysEscape = if (Properties.isMac) "⎋" else "Esc"

  override val toString = {
    val parts = serialized.split(Shortcut.SerializationDelimiter).toList 
    val newParts: List[String] =
      parts map { el =>
        el match {
          case "alt"    => sysAlt
          case "ctrl"   => sysCtrl
          case "meta"   => sysMeta
          case "shift"  => sysShift
          case "ESCAPE" => sysEscape
          case ""       => "Space"
          case _        => el
        }
      }
    val repr = newParts mkString sysSep
    println("<" + repr + ">")
    repr
  }

  lazy val serialized: String = {
    val parts =
      KeyUtils.serialize(stroke).split(
        Shortcut.SerializationDelimiter).toList
    val newParts: List[String] =
      parts flatMap { el =>
        el match {
          case "pressed" => None
          case "typed"   => None
          case _         => Some(el)
        }
      }
    newParts mkString Shortcut.SerializationDelimiter
  }
}

object Shortcut {
  
  val SerializationDelimiter = " "

  val AltName   = "alt"
  val CmdName   = "command"
  val MenuName  = "menu"
  val MetaName  = "meta"
  val CtrlName  = "ctrl"
  val ShiftName = "shift"
  val SpaceName = "space"

  def parse(str: String): Shortcut = {
    import java.lang.Character
    val stroke =
      if (str == SpaceName) KeyStroke.getKeyStroke(new Character(' '), 0)
      else KeyStroke.getKeyStroke(str)
    Shortcut(stroke)
  }.ensuring(_ != null, "could not parse keystroke: " + str)

  def normXmlKey(str: String) = {
    val noMenu =
      str match {
        case MenuName => if (Properties.isMac) MetaName else CtrlName
        case CmdName  => MetaName
        case _        => str
      }
    val norm = noMenu.toLowerCase match {
      case nm @ (AltName | MetaName | CtrlName | ShiftName) => nm
      case nm => nm.toUpperCase
    }
    println(str + " normalized to: " + norm)
    norm
  }
}
