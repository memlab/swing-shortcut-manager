package edu.upenn.psych.memory.keyboardmanager

import java.awt.{ Color, Dimension, Insets }
import java.awt.event.{ ActionEvent, KeyEvent, MouseAdapter, MouseEvent,
                        WindowAdapter, WindowEvent }
import java.util.EventObject

import javax.swing.{ Box, BoxLayout }
import javax.swing.{ AbstractAction, BorderFactory, KeyStroke,
                     ListSelectionModel, SwingUtilities, WindowConstants }
import javax.swing.{ DefaultCellEditor, JComponent, JFrame, JLabel, JPanel,
                     JTable, JScrollPane, JTextField, UIManager }
import javax.swing.event.TableModelListener
import javax.swing.table.{ TableCellRenderer, TableModel }
import javax.swing.text.JTextComponent

import scala.collection.{ mutable => m }
import scala.collection.JavaConverters._
import scala.util.Properties

class ShortcutManager(xactions: List[XAction]) extends JFrame {

  this setSize new Dimension(Scroller.getPreferredSize.getWidth.toInt,
                             Scroller.getPreferredSize.getHeight.toInt)
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

class ShortcutsTable(xactions: List[XAction]) extends RXTable {

  this setModel ShortcutsTableModel
  this setSelectionMode ListSelectionModel.SINGLE_SELECTION
  this setFillsViewportHeight true
  this addMouseListener ShortcutsMouseAdapter
  this setSelectAllForEdit true

  val header = getTableHeader()
  header setReorderingAllowed false
  header setResizingAllowed true

  override def getCellRenderer(rx: Int, cx: Int) = new ShortcutsCellRenderer
  override def getDefaultRenderer(clazz: Class[_]) = new ShortcutsCellRenderer
  override def getCellEditor(rx: Int, cx: Int) =
    new ShortcutsCellEditor(getCellRenderer(rx, cx))
  override def getDefaultEditor(clazz: Class[_]) =
    new ShortcutsCellEditor(getDefaultRenderer(clazz))

  val leftRightPad = 10

  object ShortcutsMouseAdapter extends MouseAdapter {

    override def mouseClicked(e: MouseEvent) {

    }
  }

  object ShortcutsTableModel extends TableModel {

    val headers = List("Action", "Shortcut", "Default")

    override def getRowCount = xactions.length
    override def getColumnCount = headers.length
    override def isCellEditable(rx: Int, cx: Int) = cx == 1
    override def getColumnName(cx: Int) = headers(cx)
    override def getColumnClass(cx: Int) = classOf[String]
    override def getValueAt(rx: Int, cx: Int) =
      if (rx >= xactions.length || cx >= headers.length || rx < 0 || cx < 0)
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

  class ShortcutsCellEditor(rend: JTextField) extends DefaultCellEditor(rend) {

    override def getTableCellEditorComponent(
      tab: JTable, value: AnyRef, sel: Boolean, rx: Int, cx: Int) = {
        val cmp = super.getTableCellEditorComponent(tab, value, sel, rx, cx)
        cmp match {
          case jt: JTextField => {
            jt setText value.toString
            jt setBorder BorderFactory.createEmptyBorder(0, leftRightPad,
                                                         0, leftRightPad)
          }
          case _ =>
        }
        cmp
    }
  }

  class ShortcutsCellRenderer extends JTextField with TableCellRenderer {

    override def getTableCellRendererComponent(
      tab: JTable, value: AnyRef, sel: Boolean, foc: Boolean,
      rx: Int, cx: Int) = {
        setText(value.toString)

        if (foc)
          setBorder(UIManager getBorder("Table.focusCellHighlightBorder"))
        else setBackground(tab getBackground)

        if (sel) setBackground(tab getSelectionBackground())
        else setBackground(tab.getBackground())

        // setBorder(null)
        setBorder(
          BorderFactory.createEmptyBorder(0, leftRightPad, 0, leftRightPad))

        this
    }
  }
}
