package edu.upenn.psych.memory.keyboardmanager

import java.awt.{ List => _, _ }
import java.io.{ Console => _, _ }
import javax.swing._
import org.jdom._
import org.jdom.input._

import scala.collection.{ mutable => m }
import scala.collection.JavaConverters._

class KeyboardShortcutManager(shortsFile: ShortcutsFile)  extends JFrame {
  this setSize (500, 500)
  this setDefaultCloseOperation JFrame.EXIT_ON_CLOSE
  this setTitle "Keyboard Manager"

  val scroller = new JScrollPane
  scroller setViewportView new ShortcutsPanel(shortsFile)
  scroller.getHorizontalScrollBar setUnitIncrement 15
  scroller.getVerticalScrollBar setUnitIncrement 15
  this setContentPane scroller
}

class ShortcutsPanel(shortsFile: ShortcutsFile) extends JPanel {
  this setLayout new BoxLayout(this, BoxLayout.Y_AXIS)
  for { action <- shortsFile.boundActions
        short <- action.shortcuts } {
    this add new ShortcutDisplay(action.actionName, short)
  }
}

class ShortcutsFile(inputStream: InputStream) {
  val builder = new SAXBuilder
  val doc = builder build inputStream
  val bindingsEl = doc.getRootElement
  val actionEls: List[Any] = bindingsEl.getChildren.asScala.toList
  val boundActionsBuf = new m.ListBuffer[BindableAction]()
  for (rawActionEl <- actionEls) {
    val actionEl = rawActionEl.asInstanceOf[Element]
    val actionName = actionEl getAttributeValue "name"
    val short = BindableAction(actionName, List(Shortcut(Nil, Nil)))
    boundActionsBuf append short
  }
  val boundActions = boundActionsBuf.toList
}

case class BindableAction(actionName: String, shortcuts: List[Shortcut])
case class Shortcut(masks: List[String], keys: List[String])

class ShortcutDisplay(actionName: String, short: Shortcut) extends JPanel {
  this setLayout new BoxLayout(this, BoxLayout.X_AXIS)
  this setBorder BorderFactory.createLineBorder(Color.BLACK, 3)
  this add new JLabel(actionName)
  this add new JTextField
}

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
