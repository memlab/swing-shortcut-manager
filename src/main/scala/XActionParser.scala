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

import java.net.URL

import scala.collection.{ mutable => m }
import scala.collection.JavaConverters._

import org.jdom.{ Attribute, Element }
import org.jdom.input.SAXBuilder
import org.jdom.xpath.XPath

class XActionParser(url: URL) {

  private val ClassAttr = "class"
  private val KeyNameAttr = "keyname"
  private val NameAttr = "name"
  private val TooltipAttr = "tooltip"
  private val ArgAttr = "enum"

  private val KeyName  = "key"
  private val MaskName = "mask"

  lazy val xactions: List[XAction] = {
    val acts = parseXActions()
    val shortcuts: List[Shortcut] = acts.flatMap { _.shortcut }
    val ids: List[String] = acts map { _.id }
    def assertNoDups[A](lst: List[A]) = {
      for (el <- lst) {
        if (lst.count{ el == _ } > 1)
          throw ShortcutFileFormatException(
            "shortcuts file contains duplicate: " + el
          )
      }
    }
    assertNoDups(ids)
    assertNoDups(shortcuts)
    acts
  }

  private def xpathEls(query: String, context: AnyRef): List[Element] = {
    val xpath = XPath newInstance query
    xpath.selectNodes(context).asScala.map {
      _.asInstanceOf[Element]
    }.toList
  }

  private def parseXActions(): List[XAction] = {

    val builder = new SAXBuilder
    val doc = builder build url.openStream()
    val actionEls = xpathEls("//action", doc)

    val xactions = actionEls flatMap { parseSingleXAction(_) }
    xactions toList
  }

  /* `None` is returned if any expected element (e.g., they shortcut element)
   * is not parseable, even if that element is optional. */
  private def parseSingleXAction(act: Element): Option[XAction] = {
    val name = act getAttributeValue NameAttr
    val clazz = act getAttributeValue ClassAttr
    val tooltipOpt = Option(act getAttributeValue TooltipAttr)
    val argOpt = Option(act getAttributeValue ArgAttr)
    val baseXAction = XAction(clazz, argOpt, name, tooltipOpt, None)

    if (act.getChildren.size == 0) Some(baseXAction)
    else {
      parseShortcut(act.getChildren.get(0).asInstanceOf[Element]) match {
        case opt @ Some(_) => Some(baseXAction.copy(shortcut = opt))
        case None => None
      }
    }
  }

  def parseShortcut(short: Element): Option[Shortcut] = {
    val keys = short.getChildren.asScala.map { _.asInstanceOf[Element] }
    val maskKeyEls = xpathEls(".//mask", short)
    val nonMaskKeyEls = xpathEls(".//key", short)
    def getNames(els: List[Element]) =
      els.map { _.getAttributeValue(KeyNameAttr) }
    val nonMaskKeyNames = getNames(nonMaskKeyEls)
    val maskKeyNames = getNames(maskKeyEls).map(Key external2InternalForm _)
    Shortcut fromExternalForm (maskKeyNames, nonMaskKeyNames)
  }
}

case class ShortcutFileFormatException(msg: String)
  extends RuntimeException(msg)
