package edu.upenn.psych.memory.keyboardmanager

import java.io.InputStream

import scala.collection.{ mutable => m }
import scala.collection.JavaConverters._

import org.jdom.Element
import org.jdom.input.SAXBuilder
import org.jdom.xpath.XPath

class XActionsParser(inputStream: InputStream) {

  val xactions: List[XAction] = parseStream()

  private val ClassAttr = "class"
  private val KeyAttr  = "key"
  private val KeyNameAttr = "keyname"
  private val MaskAttr = "mask"
  private val NameAttr = "name"
  private val TooltipAttr = "tooltip"

  private def parseStream(): List[XAction] = {
    val builder = new SAXBuilder
    val doc = builder build inputStream
    val xpath = XPath.newInstance("/actions/action")
    val actionEls: Seq[Element] = xpath.selectNodes(doc).asScala.map {
      _.asInstanceOf[Element]
    }
    val xactions = actionEls map { act =>
      val name = act getAttributeValue NameAttr
      val clazz = act getAttributeValue ClassAttr
      val tooltipOpt = Option(act getAttributeValue TooltipAttr)
      val shortcutOpt: Option[Shortcut] = None
      XAction(clazz, name, tooltipOpt, shortcutOpt)
    }
    xactions toList
  }
}
