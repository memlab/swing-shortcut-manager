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

  private val KeyName  = "key"
  private val MaskName = "mask"

  lazy val xactions: List[XAction] = {
    val acts = parseXActions()
    val shortcuts = acts map { _.shortcut }
    val names = acts map { _.className }
    def noDups[A](lst: List[A]) = {
      for (el <- lst) {
        if (lst.count{ el == _ } > 1)
          throw ShortcutFileFormatException(
            "shortcuts file contains duplicate: " + el
          )
      }
    }
    noDups(names)
    noDups(shortcuts)
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

    if (act.getChildren.size == 0) Some(XAction(clazz, name, tooltipOpt, None))
    else {
      parseShortcut(act.getChildren.get(0).asInstanceOf[Element]) match {
        case opt @ Some(_) => Some(XAction(clazz, name, tooltipOpt, opt))
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
