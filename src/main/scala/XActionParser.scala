package edu.upenn.psych.memory.shortcutmanager

import java.net.URL

import scala.collection.{ mutable => m }
import scala.collection.JavaConverters._
import scala.util.Properties

import org.jdom.{ Attribute, Element }
import org.jdom.input.SAXBuilder
import org.jdom.xpath.XPath

class XActionParser(url: URL) {

  private val ClassAttr = "class"
  private val KeyNameAttr = "keyname"
  private val NameAttr = "name"
  private val TooltipAttr = "tooltip"
  private val ArgAttr = "enum"
  private val OsAttr = "os"

  private val KeyName  = "key"
  private val MaskName = "mask"

  sealed trait OS
  case object Windows extends OS
  case object Mac extends OS
  case object Linux extends OS

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
    val osOpt: Option[List[String]] =
      Option(act getAttributeValue OsAttr) match {
        case Some(str) => Some(str.split(",").toList)
        case None => None
      }

    osOpt match {
      case Some(goodOSes) if goodOSes.contains(Properties.osName) == false => None
      case _ => {
        val baseXAction = XAction(clazz, argOpt, name, tooltipOpt, None)

        if (act.getChildren.size == 0) Some(baseXAction)
        else {
          parseShortcut(act.getChildren.get(0).asInstanceOf[Element]) match {
            case opt @ Some(_) => Some(baseXAction.copy(shortcut = opt))
            case None => None
          }
        }
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
