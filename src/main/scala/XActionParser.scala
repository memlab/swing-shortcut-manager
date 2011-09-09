package edu.upenn.psych.memory.keyboardmanager

import java.net.URL

import scala.collection.{ mutable => m }
import scala.collection.JavaConverters._

import org.jdom.{ Attribute, Element }
import org.jdom.input.SAXBuilder
import org.jdom.xpath.XPath

class XActionsParser(url: URL) {

  private val ClassAttr = "class"
  private val KeyNameAttr = "keyname"
  private val NameAttr = "name"
  private val TooltipAttr = "tooltip"

  private val KeyName  = "key"
  private val MaskName = "mask"

  lazy val xactions: List[XAction] = parseXActions()

  private def parseXActions(): List[XAction] = {

    def xpathEls(query: String, context: AnyRef): List[Element] = {
      val xpath = XPath newInstance query
      xpath.selectNodes(context).asScala.map {
        _.asInstanceOf[Element]
      }.toList
    }

    val builder = new SAXBuilder
    val doc = builder build url.openStream()
    val actionEls = xpathEls("//action", doc)

    def parseXAction(act: Element): XAction = {
      val name = act getAttributeValue NameAttr
      val clazz = act getAttributeValue ClassAttr
      val tooltipOpt = Option(act getAttributeValue TooltipAttr)

      def parseShortcut(short: Element): Shortcut = {
        val keys = short.getChildren.asScala.map { _.asInstanceOf[Element] }
        val maskKeyEls = xpathEls(".//mask", short)
        val nonMaskKeyEls = xpathEls(".//key", short)
        val maskKeys = maskKeyEls.map { el =>
          MaskKey(el getAttributeValue KeyNameAttr)
        }
        val nonMaskKey = {
          val el = nonMaskKeyEls head
          val keyName = el getAttributeValue KeyNameAttr
          NonMaskKey(keyName)
        }
        Shortcut(maskKeys.toList, nonMaskKey)
      }

      val shortcutOpt: Option[Shortcut] =
        if (act.getChildren.size == 0) None
        else Some(parseShortcut(act.getChildren.get(0).asInstanceOf[Element]))

      XAction(clazz, name, tooltipOpt, shortcutOpt)
    }
    val xactions = actionEls map { parseXAction(_) }
    xactions toList
  }
}
