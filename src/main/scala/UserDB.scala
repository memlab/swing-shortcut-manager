/*
This file is part of swing-keyboard-manager
<https://github.com/memlab/swing-shortcut-manager>, a shortcut manager
for Java Swing used in Penn TotalRecall
<http://memory.psych.upenn.edu/TotalRecall>

swing-keyboard-manager is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as published
by the Free Software Foundation, version 3 only.

swing-keyboard-manager is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with swing-keyboard-manager.  If not, see
<http://www.gnu.org/licenses/>.
*/

package edu.upenn.psych.memory.shortcutmanager

import java.util.prefs.Preferences

class UserDB(namespace: String, defaultXActions: List[XAction],
  listener: XActionListener) {

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
    listener xActionUpdated xaction
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
