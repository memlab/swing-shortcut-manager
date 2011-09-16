package edu.upenn.psych.memory.shortcutmanager

import java.awt.event.KeyEvent

import javax.swing.KeyStroke

case class Shortcut(stroke: KeyStroke) {

  import Shortcut.isMac

  val PCCtrl = "Ctrl"
  val PCAlt = "Alt"
  val PCShift = "Shift"
  val PCMeta = "Meta"

  val MacCtrl = "^"
  val MacOption = "⌥"
  val MacShift = "⇧"
  val MacCommand = "⌘"

  val sysSep = if (isMac) "" else "+"

  import KeyEvent._
  val MacMap: Map[String, (String, String)] = Map (
    "control" -> (PCCtrl, MacCtrl),
    "alt" -> (PCAlt, MacOption),
    "shift" -> (PCShift, MacShift),
    "meta" -> (PCMeta, MacCommand),
    "BACK_SPACE" -> ("BackSpace", "⌫"),
    "DELETE" -> ("Del", "⌦"),
    "ENTER" -> ("Enter", "↩"),
    "ESCAPE" -> ("Esc", "⎋"),
    "HOME" -> ("Home", "\u2196"),
    "END" -> ("End", "\u2198"),
    "PAGE_UP" -> ("PgUp", "PgUp"), //u8670
    "PAGE_DOWN" -> ("PgDn", "PgDn"), //u8671
    "LEFT" -> ("Left", "←"),
    "RIGHT" -> ("Right", "→"),
    "UP" -> ("Up", "↑"),
    "DOWN" -> ("Down", "↓"),
    "TAB" -> ("Tab", "Tab") //u8677
  )

  lazy val macOrder = List(MacCtrl, MacOption, MacShift, MacCommand)
  lazy val pcOrder = List(PCShift, PCCtrl, PCAlt)

  def sortKeys(a: String, b: String) = {
    val order = if (isMac) macOrder else pcOrder
    (order indexOf a, order indexOf b) match {
      case (-1, -1) => true //arbitrary choice
      case (_, -1) => true
      case (-1, _) => false
      case (i, j) => i < j
    }
  }

  lazy val internalForm: String =
    Option(UnsafeKeyUtils.getInternalFormOrNull(stroke)) match {
      case Some(str) => str
      case None => {
        sys.error (
          "sorry, I refuse to create a Shortcut whose KeyStroke has no " +
          "valid internalForm field according to UnsafeKeyUtils.java"
        )
      }
    }

  override lazy val toString = {
    val parts = Shortcut.separateInternalForm(internalForm)
    import Key._
    val newParts: List[String] = parts.map { s: String =>
      MacMap.get(s) match {
        case None => s
        case Some(Pair(pc, mac)) => if (isMac) mac else pc
      }
    }.sortWith(sortKeys)
     .filterNot{ Set("typed", "pressed", "released") contains _ }
     .map{ _.toLowerCase.capitalize }

    val repr = newParts mkString sysSep
    repr
  }
}

object Shortcut {
  
  val isMac = util.Properties.isMac
  private val InternalFormDelimiter = " "

  def separateInternalForm(internalForm: String): List[String] =
    internalForm.split(InternalFormDelimiter).toList

  def fromInternalForm(internalForm: String): Option[Shortcut] =
    Option(KeyStroke getKeyStroke internalForm) match {
      case Some(stroke) => {
        Some(Shortcut(stroke))
      }
      case None => {
        Console.err println (
          "KeyStroke.getKeyStroke could not parse allegedly internal form: " +
          internalForm
        )
        None
      }
    }

  def fromExternalForm(
    maskKeyExternalForms: List[String],
    nonMaskKeyExternalForms: List[String]): Option[Shortcut] = {
      val maskKeyInternalForms = maskKeyExternalForms map {
        Key external2InternalForm _
      }
      val nonMaskKeyInternalForms = nonMaskKeyExternalForms map {
        Key external2InternalForm _
      }
      val internalShortcutForm = (
        maskKeyInternalForms.mkString(InternalFormDelimiter) +
        InternalFormDelimiter +
        nonMaskKeyInternalForms.mkString(InternalFormDelimiter)
      )
      Shortcut fromInternalForm internalShortcutForm
  }
}

object Key {

  import Shortcut.isMac

  private val ExternalMenu = "menu"
  private val ExternalCommand = "command"

  val InternalAlt = "alt"
  val InternalCtrl = "ctrl"
  val InternalEscape = "ESCAPE"
  val InternalMeta = "meta"
  val InternalShift = "shift"

  def external2InternalForm(str: String): String = {
    str match {
      case ExternalMenu => if (isMac) InternalMeta else InternalCtrl
      case ExternalCommand => InternalMeta
      case _ => str
    }
  }
}
