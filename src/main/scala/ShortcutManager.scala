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

class ShortcutManager(xactions: List[XAction]) extends JFrame {

  this setSize new Dimension(Scroller.getPreferredSize.getWidth.toInt, 500)
  this setDefaultCloseOperation WindowConstants.DO_NOTHING_ON_CLOSE
  this addWindowListener EscapeWindowListener
  this setTitle "Keyboard Shortcuts Manager"
  this setContentPane Scroller

  object Scroller extends JScrollPane {

    this setViewportView new ShortcutsTable(xactions)
    getHorizontalScrollBar setUnitIncrement 15
    getVerticalScrollBar setUnitIncrement 15

    val Exit = "exit"
    val EscStroke = KeyStroke getKeyStroke (KeyEvent.VK_ESCAPE, 0, false)
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(EscStroke, Exit)
    getActionMap().put(Exit, new AbstractAction() {
      val event = new WindowEvent(ShortcutManager.this,
                                  WindowEvent.WINDOW_CLOSING)

      override def actionPerformed(e: ActionEvent) =
        EscapeWindowListener windowClosing event
    })
  }

  object EscapeWindowListener extends WindowAdapter {

    override def windowClosing(e: WindowEvent) =
      ShortcutManager.this setVisible false
  }
}

class ShortcutsTable(xactions: List[XAction]) extends JTable {

  this setModel ShortcutsTableModel
  this setSelectionMode ListSelectionModel.SINGLE_SELECTION
  this setBorder BorderFactory.createLineBorder(Color.BLACK, 1)
  val header = getTableHeader()
  header setReorderingAllowed false
  header setResizingAllowed true
  header setBorder BorderFactory.createLineBorder(Color.BLACK, 1)

  object ShortcutsTableModel extends TableModel {
    val headers = List("Action", "Shortcut", "Default")

    override def getRowCount = xactions.length
    override def getColumnCount = headers.length
    override def isCellEditable(rx: Int, cx: Int) = cx == 1
    override def getColumnName(cx: Int) = headers(cx)
    override def getColumnClass(cx: Int) = classOf[String]
    override def getValueAt(rx: Int, cx: Int) =
      if (rx >= xactions.length || cx >= headers.length || rx < 0 || cx < 0 )
        null
      else {
        val xaction = xactions(rx)
        if (cx == 0) xaction.name
        else if (cx == 1) xaction.shortcut.getOrElse("")
        else xaction.shortcut.getOrElse("")
      }
    override def setValueAt(value: AnyRef, rx: Int, cx: Int) = ()
    override def addTableModelListener(l: TableModelListener) = ()
    override def removeTableModelListener(l: TableModelListener) = ()
  }
}
