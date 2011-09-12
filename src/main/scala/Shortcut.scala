package edu.upenn.psych.memory.keyboardmanager

import javax.swing.KeyStroke

import scala.util.Properties


case class Shortcut(stroke: KeyStroke) {

  // val sysSep   = if (Properties.isMac) ""  else "+"

  // val sysAlt    = if (Properties.isMac) "⌥" else "Alt"
  // val sysCtrl   = if (Properties.isMac) "^" else "Ctrl"
  // val sysMeta   = if (Properties.isMac) "⌘" else "Meta"
  // val sysShift  = if (Properties.isMac) "⇧" else "Shift"
  // val sysEscape = if (Properties.isMac) "⎋" else "Esc"

  val internalForm: String =
    Option(UnsafeKeyUtils.getInternalFormOrNull(stroke)) match {
      case Some(str) => str
      case None => {
        sys.error (
          "sorry, I refuse to create a Shortcut whose KeyStroke has no " +
          "valid internalForm field according to UnsafeKeyUtils.java"
        )
      }
    }

  // val sysForm = {
  //   val parts = serialized.split(Shortcut.SerializationDelimiter).toList 
  //   val newParts: List[String] =
  //     parts map { el =>
  //       el match {
  //         case "alt"    => sysAlt
  //         case "ctrl"   => sysCtrl
  //         case "meta"   => sysMeta
  //         case "shift"  => sysShift
  //         case "ESCAPE" => sysEscape
  //         case ""       => "Space"
  //         case _        => el
  //       }
  //     }
  //   val repr = newParts mkString sysSep
  //   repr
  // }

    // val parts =
    //   KeyUtils.serialize(stroke).split(
    //     Shortcut.SerializationDelimiter).toList
    // val newParts: List[String] =
    //   parts flatMap { el =>
    //     el match {
    //       case "pressed" => None
    //       case "typed"   => None
    //       case _         => Some(el)
    //     }
    //   }
    // newParts mkString Shortcut.SerializationDelimiter
  // }
}

object Shortcut {
  
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
      val InternalFormDelimiter = " "
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

  private val Meta = "meta"

  def external2InternalForm(str: String): String = {
    str match {
      case "menu"     => if (Properties.isMac) Meta else "ctrl"
      case "command"  => Meta
      case _          => str
    }
  }
}
