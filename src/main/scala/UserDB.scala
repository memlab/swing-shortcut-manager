package edu.upenn.psych.memory.keyboardmanager

import java.util.prefs.Preferences

class UserDB(namespace: String, defaultXActions: List[XAction]) {
  require(namespace startsWith "/",
          "namespace " + namespace + " is not absolute")

  private val NoShortcut = "#"

  private val prefs = Preferences.userRoot.node(namespace)
  
  private def store(xaction: XAction) {
    val key = xaction.className
    val value = xaction.shortcut match {
      case Some(short) => short.internalForm
      case None    => NoShortcut
    }
    prefs put (key, value)
  }

  private def retrieve(className: String): Option[Shortcut] = {
    val key = className
    Option(prefs get (key, null)) match {
      case Some(NoShortcut) | None => None
      case Some(storedStr)       => {
        Shortcut.fromInternalForm(storedStr) match {
          case opt @ Some(_) => opt
          case None => {
            Console.err.println(getClass().getName +
                                "won't retrieve() unparseable: " + storedStr)
            None
          }
        }
      }
    }
  }

  def persistDefaults(overwrite: Boolean) {
    defaultXActions.foreach { xact =>
      retrieve(xact.className) match {
        case Some(_) if overwrite == false =>
        case _       => store(xact)
      }
    }
  }

  def retrieveAll(): Map[String, Option[Shortcut]] = {
    val classNames = defaultXActions map { _.className }
    val pairs: List[(String, Option[Shortcut])] =
      classNames map { name => name -> retrieve(name) }
    Map(pairs: _*)
  }
}

