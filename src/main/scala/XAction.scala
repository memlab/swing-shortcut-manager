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

case class XAction(val className: String,
                   enum: Option[String],
                   name: String,
                   tooltip: Option[String],
                   shortcut: Option[Shortcut]) {

  val javaTooltip = tooltip.getOrElse(null)
  val javaShortcut = shortcut.getOrElse(null)
  val javaEnum = enum.getOrElse(null)

  val id = {
    enum match {
      case Some(str) => List(className, str).mkString("-")
      case None => className
    }
  }
}
