package edu.upenn.psych.memory.shortcutmanager

import javax.swing.KeyStroke

import scala.util.Properties


case class Shortcut(stroke: KeyStroke) {

  private def ifMac[A](macThing: A, winLinThing: A) =
    if (Properties.isMac) macThing else winLinThing

  val PCCtrl = "Ctrl"
  val PCAlt = "Alt"
  val PCShift = "Shift"

  val MacCtrl = "^"
  val MacOption = "⌥"
  val MacShift = "⇧"
  val MacCommand = "⌘"

  lazy val sysSep    = ifMac("", "+")
  lazy val sysAlt    = ifMac(MacOption, PCAlt)
  lazy val sysCtrl   = ifMac(MacCtrl, PCCtrl)
  lazy val sysMeta   = ifMac(MacCommand, "Meta")
  lazy val sysShift  = ifMac(MacShift, PCShift)
  lazy val sysEscape = ifMac("⎋", "Esc")

  lazy val macOrder = List(MacCtrl, MacOption, MacShift, MacCommand)
  lazy val pcOrder = List(PCShift, PCCtrl, PCAlt)

  def sortKeys(a: String, b: String) = {
    val order = ifMac(macOrder, pcOrder)
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
    val newParts: List[String] = {
      parts map { el =>
        el match {
          case InternalAlt    => sysAlt
          case InternalCtrl   => sysCtrl
          case InternalMeta   => sysMeta
          case InternalShift  => sysShift
          case InternalEscape => sysEscape
          case _              => el
        }
      }
    }.sortWith(sortKeys)
     .filterNot{ Set("typed", "pressed", "released") contains _ }
     .map{ _.toLowerCase.capitalize }

    val repr = newParts mkString sysSep
    repr
  }
}

object Shortcut {
  
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

  private val ExternalMenu = "menu"
  private val ExternalCommand = "command"

  val InternalAlt = "alt"
  val InternalCtrl = "ctrl"
  val InternalEscape = "ESCAPE"
  val InternalMeta = "meta"
  val InternalShift = "shift"

  def external2InternalForm(str: String): String = {
    str match {
      case ExternalMenu => if (Properties.isMac) InternalMeta else InternalCtrl
      case ExternalCommand => InternalMeta
      case _ => str
    }
  }
}
