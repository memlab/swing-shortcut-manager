package edu.upenn.psych.memory.keyboardmanager

import java.io.InputStream

import scala.collection.{ mutable => m }
import scala.collection.JavaConverters._

import org.jdom.Element
import org.jdom.input.SAXBuilder
import org.jdom.xpath.XPath

class XActionsParser(inputStream: InputStream) {

  val xactions: List[XAction] = parseXActions()

  private val ClassAttr = "class"
  private val KeyNameAttr = "keyname"
  private val NameAttr = "name"
  private val TooltipAttr = "tooltip"

  private val KeyName  = "key"
  private val MaskName = "mask"

  private def parseXActions(): List[XAction] = {
    val builder = new SAXBuilder
    val doc = builder build inputStream
    val xpath = XPath.newInstance("//action")
    val actionEls: Seq[Element] = xpath.selectNodes(doc).asScala.map {
      _.asInstanceOf[Element]
    }
    def parseXAction(act: Element): XAction = {
      val name = act getAttributeValue NameAttr
      val clazz = act getAttributeValue ClassAttr
      val tooltipOpt = Option(act getAttributeValue TooltipAttr)

      def parseShortcut(short: Element): Shortcut = {
        val keys = short.getChildren.asScala.map { _.asInstanceOf[Element] }
        val (maskKeyEls, nonMaskKeyEls) =
          keys.partition { _.getName == MaskName }
        val maskKeys = maskKeyEls.map { el =>
          val keyName = el getAttributeValue KeyNameAttr
          MaskKey(keyName)
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
    xactions.foreach(println)

    sys.exit()

    xactions toList
  }
}
