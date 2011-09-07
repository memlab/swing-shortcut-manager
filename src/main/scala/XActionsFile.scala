package edu.upenn.psych.memory.keyboardmanager

import java.io.InputStream

import scala.collection.{ mutable => m }
import scala.collection.JavaConverters._

import org.jdom.Element
import org.jdom.input.SAXBuilder

class XActionsFile(inputStream: InputStream) {

  val NameAttr = "name"
  val MaskAttr = "mask"
  val KeyAttr  = "key"

  val boundActions: List[XAction] = parseStream()

  private def parseStream(): List[XAction] = {
    val builder = new SAXBuilder
    val doc = builder build inputStream
    val bindingsEl = doc.getRootElement
    val actionEls: List[Any] = bindingsEl.getChildren.asScala.toList
    val boundActionsBuf = new m.ListBuffer[XAction]()
    for (rawActionEl <- actionEls) {
      val actionEl = rawActionEl.asInstanceOf[Element]
      val actionName = actionEl getAttributeValue NameAttr
      val shortsBuf = new m.ListBuffer[Shortcut]
      val bindingChildren = actionEl.getChildren.asScala
      if (bindingChildren.nonEmpty) {
        for (rawBindingEl <- bindingChildren) {
          val bindingEl = rawBindingEl.asInstanceOf[Element]
          val maskBuf = new m.ListBuffer[Mask]()
          val keyBuf = new m.ListBuffer[NonMask]()
          for (rawMaskOrKeyEl <- bindingEl.getChildren.asScala) {
            val maskOrKeyEl = rawMaskOrKeyEl.asInstanceOf[Element]
            val attrName = maskOrKeyEl getAttributeValue NameAttr
            maskOrKeyEl.getName match {
              case KeyAttr  => keyBuf  append NonMask(attrName)
              case MaskAttr => maskBuf append Mask(attrName)
            }
          }
          val short = Shortcut(maskBuf.toList, keyBuf.toList)
          shortsBuf append short
        }
      }
      else shortsBuf append Shortcut(Nil, Nil)
      // boundActionsBuf append BoundAction(actionName, shortsBuf.toList)
    }
    boundActionsBuf.toList
  }
}
