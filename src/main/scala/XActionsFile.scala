package edu.upenn.psych.memory.keyboardmanager

import java.io.InputStream

import scala.collection.{ mutable => m }
import scala.collection.JavaConverters._

import org.jdom.Element
import org.jdom.input.SAXBuilder

class XActionsFile(inputStream: InputStream) {

  val xactions: List[XAction] = parseStream()

  private val NameAttr = "name"
  private val MaskAttr = "mask"
  private val KeyAttr  = "key"

  private def parseStream(): List[XAction] = {
    val builder = new SAXBuilder
    val doc = builder build inputStream
    val bindingsEl = doc.getRootElement
    val actionEls: List[Any] = bindingsEl.getChildren.asScala.toList
    val xactionsBuf = new m.ListBuffer[XAction]()
    for (rawActionEl <- actionEls) {
      val actionEl = rawActionEl.asInstanceOf[Element]
      val actionName = actionEl getAttributeValue NameAttr
      val shortsBuf = new m.ListBuffer[Shortcut]
      val bindingChildren = actionEl.getChildren.asScala
      var shortcut: Option[Shortcut] = None
      if (bindingChildren.nonEmpty) {
        val bindingEl = bindingChildren.head.asInstanceOf[Element]
        val maskBuf = new m.ListBuffer[MaskKey]()
        var keyOpt: Option[NonMaskKey] = None
        for (rawMaskOrKeyEl <- bindingEl.getChildren.asScala) {
          val maskOrKeyEl = rawMaskOrKeyEl.asInstanceOf[Element]
          val attrName = maskOrKeyEl getAttributeValue NameAttr
          maskOrKeyEl.getName match {
            case KeyAttr  => keyOpt = Some(NonMaskKey(attrName))
            case MaskAttr => maskBuf append MaskKey(attrName)
            case e => println(e)
          }
          if (maskBuf.nonEmpty) {
            keyOpt match {
              case Some(key) => shortcut = Some(Shortcut(maskBuf.toList, key))
              case None =>
            }
          }
        }
      }
      val xaction =
        XAction("clazz", "name", Some("tooltip"), shortcut)
      xactionsBuf append xaction
    }
    xactionsBuf.toList
  }
}
