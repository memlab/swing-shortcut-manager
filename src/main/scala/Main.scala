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

object Main {

  def main(args: Array[String]) = {
    val namespace = "/" + getClass.getPackage.getName.replace(".", "/")
    val resourcePath = "/actions.xml"
    Option(Main.getClass.getResource(resourcePath)) match {
      case Some(url) => {
        val listener = new XActionListener() {
          override def xActionUpdated(xaction: XAction) =
            println("heard " + xaction)
        }
        new ShortcutManager(url, namespace, listener).setVisible(true)
      }
      case None => Console.err println "no keyboard shortcuts file found"
    }
  }
}

