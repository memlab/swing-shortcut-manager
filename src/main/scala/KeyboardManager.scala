package edu.upenn.psych.memory.keyboardmanager

import java.awt.{ List => _, _ }
import java.awt.event._
import java.io.{ Console => _, _ }
import javax.swing._
import org.jdom._
import org.jdom.input._

import scala.collection.{ mutable => m }
import scala.collection.JavaConverters._

class KeyboardShortcutManager(shortsFile: ShortcutsFile) extends JFrame {

  this setSize (500, 500)
  this setDefaultCloseOperation WindowConstants.DO_NOTHING_ON_CLOSE
  this addWindowListener EscapeWindowListener
  this setTitle "Keyboard Shortcuts Manager"
  this setContentPane Scroller

  object Scroller extends JScrollPane {

    this setViewportView new ShortcutsPanel(shortsFile)
    getHorizontalScrollBar setUnitIncrement 15
    getVerticalScrollBar setUnitIncrement 15
    
    val Exit = "exit"
    val EscStroke = KeyStroke getKeyStroke (KeyEvent.VK_ESCAPE, 0, false)
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(EscStroke, Exit)
    getActionMap().put(Exit, new AbstractAction() {
      val event = new WindowEvent(KeyboardShortcutManager.this,
                                  WindowEvent.WINDOW_CLOSING)

      override def actionPerformed(e: ActionEvent) =
        EscapeWindowListener windowClosing event
    })
  }

  object EscapeWindowListener extends WindowAdapter {

    override def windowClosing(e: WindowEvent) =
      KeyboardShortcutManager.this.setVisible(false)
  }
}

class ShortcutsPanel(shortsFile: ShortcutsFile) extends JPanel {

  this setLayout new BoxLayout(this, BoxLayout.Y_AXIS)
  for { action <- shortsFile.boundActions
        short <- action.shortcuts } {
    this add new ShortcutDisplay(action.actionName, short)
  }
}

class ShortcutsFile(inputStream: InputStream) {

  val NameAttr = "name"
  val MaskAttr = "mask"
  val KeyAttr  = "key"

  val boundActions: List[BoundAction] = parseStream()

  private def parseStream(): List[BoundAction] = {
    val builder = new SAXBuilder
    val doc = builder build inputStream
    val bindingsEl = doc.getRootElement
    val actionEls: List[Any] = bindingsEl.getChildren.asScala.toList
    val boundActionsBuf = new m.ListBuffer[BoundAction]()
    for (rawActionEl <- actionEls) {
      val actionEl = rawActionEl.asInstanceOf[Element]
      val actionName = actionEl getAttributeValue NameAttr
      val shortsBuf = new m.ListBuffer[Shortcut]
      for (rawBindingEl <- actionEl.getChildren.asScala) {
        val bindingEl = rawBindingEl.asInstanceOf[Element]
        val maskBuf = new m.ListBuffer[Mask]()
        val keyBuf = new m.ListBuffer[Key]()
        for (rawMaskOrKeyEl <- bindingEl.getChildren.asScala) {
          val maskOrKeyEl = rawMaskOrKeyEl.asInstanceOf[Element]
          val attrName = maskOrKeyEl getAttributeValue NameAttr
          maskOrKeyEl.getName match {
            case KeyAttr  => keyBuf  append Key(attrName)
            case MaskAttr => maskBuf append Mask(attrName)
          }
        }
        val short = Shortcut(maskBuf.toList, keyBuf.toList)
        shortsBuf append short
      }
      boundActionsBuf append BoundAction(actionName, shortsBuf.toList)
    }
    boundActionsBuf.toList
  }
}

class ShortcutDisplay(actionName: String, short: Shortcut) extends JPanel {

  this setLayout new BoxLayout(this, BoxLayout.X_AXIS)
  this setBorder BorderFactory.createLineBorder(Color.BLACK, 3)
  this add new JLabel(actionName)
  this add new JTextField
}

case class BoundAction(actionName: String, shortcuts: List[Shortcut])
case class Shortcut(masks: List[Mask], keys: List[Key])
sealed trait KeyElement
case class Mask(name: String) extends KeyElement
case class Key(name: String) extends KeyElement


object Main {

  def main(args: Array[String]) = {
    val resourcePath = "/keyboard-shortcuts.xml"
    Option(Main.getClass.getResourceAsStream(resourcePath)) match {
      case Some(stream) => {
        val shortsFile = new ShortcutsFile(stream)
        val mgr = new KeyboardShortcutManager(shortsFile)
        mgr setVisible true
      }
      case None => Console.err println "no keyboard shortcuts file found"
    }
  }
}
