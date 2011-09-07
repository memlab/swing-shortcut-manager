package edu.upenn.psych.memory.keyboardmanager

import scala.util.Properties

sealed trait AbstractKey {
  def keyRepr: String
  def order: Int
}

case class NonMaskKey(name: String) extends AbstractKey {
  override val keyRepr = name
  override val order = 0
}

sealed trait MaskKey extends AbstractKey {
  def xmlName: String
}

object MaskKey {

  def apply(name: String) = name match {
    case AltKey.xmlName     => AltKey
    case CommandKey.xmlName => CommandKey
    case ControlKey.xmlName => ControlKey
    case ShiftKey.xmlName   => ShiftKey
    case WinKey.xmlName     => WinKey
    case "menu"             => if (Properties.isMac) CommandKey else ControlKey
    case _                  => sys.error("unknown mask key: " + name)
  }
}

case object AltKey extends MaskKey {
  override val keyRepr = if (Properties.isMac) "⌥" else "Alt"
  override val xmlName = "alt"
  override val order = -3
}

case object CommandKey extends MaskKey {
  override val keyRepr = "⌘"
  override val xmlName = "command"
  override val order = -1
}

case object ControlKey extends MaskKey {
  override val keyRepr = if (Properties.isMac) "^" else "Ctrl"
  override val xmlName = "control"
  override val order = -4
}

case object ShiftKey extends MaskKey {
  override val keyRepr = if (Properties.isMac) "⇧" else "Shift"
  override val xmlName = "shift"
  override val order = -2
}

case object WinKey extends MaskKey {
  override val keyRepr = if (Properties.osName == "Linux") "Mod4" else "Win"
  override val xmlName = "winkey"
  override val order = -5
}


case class Shortcut(masks: List[MaskKey], key: NonMaskKey) {

  val sep = if (Properties.isMac) "" else "+"

  override val toString = {

    def keyListRepr(lst: List[AbstractKey]) = {
      val sorted = lst sortWith { (f, s) => f.order < s.order }
      val reprs = sorted map { _.keyRepr }
      reprs mkString sep
    }

    val masksStr = keyListRepr(masks)
    masksStr + { if(masksStr.length == 0) "" else sep } + key.keyRepr
  }
}
