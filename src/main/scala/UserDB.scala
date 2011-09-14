package edu.upenn.psych.memory.shortcutmanager

import java.util.prefs.Preferences

class UserDB(namespace: String, defaultXActions: List[XAction]) {
  require(namespace startsWith "/",
          "namespace " + namespace + " is not absolute")

  private val NoShortcut = "#"

  private val prefs = Preferences.userRoot.node(namespace)
  
  def store(xaction: XAction) {
    val key = xaction.id
    val value = xaction.shortcut match {
      case Some(short) => short.internalForm
      case None    => NoShortcut
    }
    prefs put (key, value)
  }

  def retrieve(id: String): Option[Shortcut] = {
    val key = id
    Option(prefs get (key, null)) match {
      case Some(NoShortcut) | None => None
      case Some(storedStr)       => {
        Shortcut.fromInternalForm(storedStr) match {
          case opt @ Some(_) => opt
          case None => {
            Console.err.println(getClass().getName +
                                " won't retrieve() unparseable: " + storedStr)
            None
          }
        }
      }
    }
  }

  def persistDefaults(overwrite: Boolean) {
    defaultXActions.foreach { xact =>
      retrieve(xact.id) match {
        case Some(_) if overwrite == false =>
        case _       => store(xact)
      }
    }
  }

  def retrieveAll(): Map[String, Option[Shortcut]] = {
    val ids = defaultXActions map { _.id }
    val pairs: List[(String, Option[Shortcut])] =
      ids map { id => id -> retrieve(id) }
    Map(pairs: _*)
  }
}
