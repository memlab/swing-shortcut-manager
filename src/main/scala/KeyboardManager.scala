package edu.upenn.psych.memory.keyboardmanager

import java.awt.{ Color, Dimension }
import java.awt.event.{ ActionEvent, KeyEvent, WindowAdapter, WindowEvent }

import javax.swing.{ Box, BoxLayout }
import javax.swing.{ AbstractAction, BorderFactory, KeyStroke,
                     ListSelectionModel, WindowConstants }
import javax.swing.{ JComponent, JFrame, JLabel, JPanel, JTable, JScrollPane,
                     JTextField }
import javax.swing.table.TableModel
import javax.swing.event.TableModelListener

import scala.collection.{ mutable => m }
import scala.collection.JavaConverters._
import scala.util.Properties

class KeyboardShortcutManager(shortsFile: XActionsFile) extends JFrame {

  this setSize (500, 500)
  this setDefaultCloseOperation WindowConstants.DO_NOTHING_ON_CLOSE
  this addWindowListener EscapeWindowListener
  this setTitle "Keyboard Shortcuts Manager"
  this setContentPane Scroller

  object Scroller extends JScrollPane {

    this setViewportView new ShortcutsTable(shortsFile)
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
      KeyboardShortcutManager.this setVisible false
  }
}

class ShortcutsTable(shortsFile: XActionsFile) extends JTable {

  this setModel ShortcutsTableModel
  this setSelectionMode ListSelectionModel.SINGLE_SELECTION
  this setBorder BorderFactory.createLineBorder(Color.BLACK, 1)
  val header = getTableHeader()
  header setReorderingAllowed false
  header setResizingAllowed true
  header setBorder BorderFactory.createLineBorder(Color.BLACK, 1)

  object ShortcutsTableModel extends TableModel {
    val headers = List("Action", "Shortcut", "Default")
    val contents = new m.ListBuffer[m.ListBuffer[JLabel]]()
    for { action <- shortsFile.boundActions
          short <- action.shortcut } {
      // val buf = new m.ListBuffer[JLabel]()
      // buf ++= List(new JLabel(action.actionName),
      //             new JLabel(short.toString),
      //             new JLabel(short.toString))
      // contents append buf
    }

    override def getRowCount = contents.length
    override def getColumnCount = headers.length
    override def isCellEditable(rx: Int, cx: Int) = cx == 1
    override def getColumnName(cx: Int) = headers(cx)
    override def getColumnClass(cx: Int) = classOf[String]
    override def getValueAt(rx: Int, cx: Int) =
      if (rx >= contents.length || cx >= contents(rx).length) null
      else contents(rx)(cx).getText
    override def setValueAt(value: AnyRef, rx: Int, cx: Int) =
      if (! (rx >= contents.length || cx >= contents(rx).length))
        contents(rx)(cx) = new JLabel(value.toString)
    override def addTableModelListener(l: TableModelListener) = ()
    override def removeTableModelListener(l: TableModelListener) = ()
  }
}

class ShortcutDisplay(actionName: String, short: Shortcut) extends JPanel {

  this setLayout new BoxLayout(this, BoxLayout.X_AXIS)
  this setBorder BorderFactory.createLineBorder(Color.BLACK, 1)
  this add new ActionLabel
  this add Box.createHorizontalGlue
  this add new KeyField
  this setMaximumSize new Dimension(Integer.MAX_VALUE,
                                    this.getPreferredSize.getHeight.toInt)

  class ActionLabel() extends JLabel(actionName) {

    this setBorder BorderFactory.createLineBorder(Color.BLACK, 1)
  }

  class KeyField() extends JTextField {

    this setText short.toString
    this setBorder BorderFactory.createLineBorder(Color.BLACK, 1)
    this setMaximumSize new Dimension(this.getPreferredSize.getWidth.toInt,
                                      this.getPreferredSize.getWidth.toInt)
  }
}


case class XAction(val className: String,
                   actionName: Option[String],
                   tooltip: Option[String],
                   shortcut: Option[Shortcut])

case class Shortcut(masks: List[Mask], keys: List[NonMask]) {

  val sep = if (Properties.isMac) "" else "+"

  override val toString = {

    def keyListRepr(lst: List[AbstractKey]) = {
      val sorted = lst sortWith { (f, s) => f.order < s.order }
      val reprs = sorted map { _.keyRepr }
      reprs mkString sep
    }

    val masksStr = keyListRepr(masks)
    val keysStr = keyListRepr(keys)
    masksStr + { if(masksStr.length == 0) "" else sep } + keysStr
  }
}

sealed trait AbstractKey {
  def keyRepr: String
  def order: Int
}

case class NonMask(name: String) extends AbstractKey {
  override val keyRepr = name
  override val order = 0
}

sealed trait Mask extends AbstractKey {
  def xmlName: String
}

object Mask {

  def apply(name: String) = name match {
    case AltKey.xmlName     => AltKey
    case CommandKey.xmlName => CommandKey
    case ControlKey.xmlName => ControlKey
    case ShiftKey.xmlName   => ShiftKey
    case WinKey.xmlName     => WinKey
    case "menu"             => if (Properties.isMac) CommandKey else ControlKey
    case _                  => sys.error("unknown mask key: " + name)
  }
}

case object AltKey extends Mask {
  override val keyRepr = if (Properties.isMac) "⌥" else "Alt"
  override val xmlName = "alt"
  override val order = -3
}

case object CommandKey extends Mask {
  override val keyRepr = "⌘"
  override val xmlName = "command"
  override val order = -1
}

case object ControlKey extends Mask {
  override val keyRepr = if (Properties.isMac) "^" else "Ctrl"
  override val xmlName = "control"
  override val order = -4
}

case object ShiftKey extends Mask {
  override val keyRepr = if (Properties.isMac) "⇧" else "Shift"
  override val xmlName = "shift"
  override val order = -2
}

case object WinKey extends Mask {
  override val keyRepr = if (Properties.osName == "Linux") "Mod4" else "Win"
  override val xmlName = "winkey"
  override val order = -5
}


object Main {

  def main(args: Array[String]) = {
    val resourcePath = "/actions.xml"
    Option(Main.getClass.getResourceAsStream(resourcePath)) match {
      case Some(stream) => {
        val shortsFile = new XActionsFile(stream)
        val mgr = new KeyboardShortcutManager(shortsFile)
        mgr setVisible true
      }
      case None => Console.err println "no keyboard shortcuts file found"
    }
  }
}
