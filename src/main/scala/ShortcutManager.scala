package edu.upenn.psych.memory.shortcutmanager

import java.awt.{ Color, Dimension, Insets }
import java.awt.event.{ ActionEvent, KeyAdapter, KeyEvent, InputEvent,
                        WindowAdapter, WindowEvent }
import java.net.URL
import java.util.EventObject

import javax.swing.{ Box, BoxLayout }
import javax.swing.{ AbstractAction, BorderFactory, KeyStroke, JOptionPane,
                     ListSelectionModel, ScrollPaneConstants, SwingUtilities,
                     WindowConstants }
import javax.swing.{ JButton, JComponent, JFrame,
                     JLabel, JPanel, JTable, JScrollPane, UIManager }
import javax.swing.border.CompoundBorder
import javax.swing.event.TableModelListener
import javax.swing.table.{ DefaultTableCellRenderer, TableCellRenderer,
                           TableModel }
import javax.swing.text.JTextComponent

import scala.collection.{ mutable => m }
import scala.collection.JavaConverters._
import scala.util.Properties

class ShortcutManager(url: URL, namespace: String) extends JFrame {

  private val defaultXActions = new XActionParser(url).xactions
  val userdb = new UserDB(namespace, defaultXActions)
  userdb persistDefaults false

  this setSize(
    new Dimension(
      800,
      ContentPane.getPreferredSize.getWidth.toInt))
  this setDefaultCloseOperation WindowConstants.DO_NOTHING_ON_CLOSE
  this addWindowListener EscapeWindowListener
  this setTitle "Keyboard Shortcuts Manager"
  this setContentPane ContentPane

  object ContentPane extends JPanel {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
    add(Scroller)
    add(Box.createVerticalBox)
    add(ResetButtonPanel)

    object Scroller extends JScrollPane {

      this setViewportView new ShortcutTable(defaultXActions.toIndexedSeq,
                                              userdb)

      this.setHorizontalScrollBarPolicy(
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
      )
      this.setVerticalScrollBarPolicy(
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
      )
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

    object ResetButtonPanel extends JPanel {
      val but = new JButton(new AbstractAction("reset to defaults") {
        override def actionPerformed(e: ActionEvent) {
          val res =
            JOptionPane.showConfirmDialog(ShortcutManager.this,
                                          "Reset all shortcuts to defaults?")
          if (res == JOptionPane.YES_OPTION) {
            println("resetting shortcuts to defaults")
          }
        }
      })
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
      add(Box.createHorizontalGlue)
      add(but)
      add(Box.createHorizontalGlue)
    }
  }

  object EscapeWindowListener extends WindowAdapter {
    override def windowClosing(e: WindowEvent) =
      ShortcutManager.this setVisible false
  }
}

class ShortcutTable(defaultXActions: IndexedSeq[XAction],
                     userdb: UserDB) extends JTable {

  val leftRightPad = 10

  this setModel ShortcutTableModel
  this setSelectionMode ListSelectionModel.SINGLE_SELECTION
  this setFillsViewportHeight true
  this addKeyListener ShortcutKeyAdapter

  for (c <- 0 until getColumnCount) {
    val col = getColumnModel.getColumn(c)
    val maxWidth = (0 until getRowCount).map { r =>
      val renderer = getCellRenderer(r, c)
      val value = getValueAt(r, c)
      val cmp =
        renderer.getTableCellRendererComponent(
          this, value, false, false, r, c)
      cmp.getPreferredSize.getWidth.toInt
    }.max
    col setMinWidth (maxWidth + 4 + 2 * leftRightPad)
  }

  val header = getTableHeader()
  header setReorderingAllowed false
  header setResizingAllowed true

  override def getCellRenderer(rx: Int, cx: Int) = new ShortcutCellRenderer
  override def getDefaultRenderer(clazz: Class[_]) = new ShortcutCellRenderer

  object ShortcutKeyAdapter extends KeyAdapter {

    private val tab = ShortcutTable.this
    private val downKeys = new m.ListBuffer[Int]()
    private val maskCodes = Set(KeyEvent.VK_ALT, KeyEvent.VK_CONTROL,
                        KeyEvent.VK_SHIFT, KeyEvent.VK_META)
    private val standaloneKeys = Set(KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT)

    private def downKeyPartition() =
      downKeys.toList.partition { maskCodes contains _ }

    private def visualize(): String = {
      downKeys.toList.map { key =>
        Shortcut(KeyStroke.getKeyStroke(key, 0)).toString
      }
    }.toString


    private def isValidShortcut(codes: List[Int]) = {
      val (maskKeys, nonMaskKeys) = downKeyPartition()
      if (nonMaskKeys.length == 1) {
        val atLeastOneNonShiftMask = {
          val nonShifts = maskKeys.filterNot { _ == KeyEvent.VK_SHIFT }
          nonShifts.length > 0
        }
        val standalone = standaloneKeys.contains(nonMaskKeys.head)
        atLeastOneNonShiftMask || standalone
      }
      else false
    }

    override def keyPressed(e: KeyEvent) {
      val rs = tab.getSelectedRow()
      if (rs >= 0) {
        tab.getModel.getValueAt(rs, 1) match {
          case Shortcut(stroke) => {
            val code = e.getKeyCode
            downKeys append code
            if (isValidShortcut(downKeys.toList)) {
              val (maskKeys, nonMaskKeys) = downKeyPartition()
              val masks =
                maskKeys map { keyCode: Int =>
                  keyCode match {
                    case KeyEvent.VK_ALT     => InputEvent.ALT_DOWN_MASK
                    case KeyEvent.VK_CONTROL => InputEvent.CTRL_DOWN_MASK
                    case KeyEvent.VK_META    => InputEvent.META_DOWN_MASK
                    case KeyEvent.VK_SHIFT   => InputEvent.SHIFT_DOWN_MASK
                  }
                }
              val modifier = masks.fold(0) { _ | _ }
              val newStroke =
                KeyStroke.getKeyStroke(nonMaskKeys.head, modifier)
              val short = Shortcut(newStroke)
              println("binding: " + short)
              downKeys.clear()
            }
          }
          case _ =>
        }
      }
    }

    override def keyReleased(e: KeyEvent): Unit = downKeys.clear()
  }

  object ShortcutTableModel extends TableModel {

    val headers = List("Action", "Shortcut", "Default")
    val NoShortcutRepr = ""

    override def getRowCount = defaultXActions.length
    override def getColumnCount = headers.length
    override def isCellEditable(rx: Int, cx: Int) = false
    override def getColumnName(cx: Int) = headers(cx)
    override def getColumnClass(cx: Int) = classOf[String]
    override def getValueAt(rx: Int, cx: Int) =
      if (rx >= defaultXActions.length || cx >= headers.length ||
          rx < 0 || cx < 0) null
      else {
        val defXAction: XAction = defaultXActions(rx)
        val curXShortcutOpt: Option[Shortcut] =
          userdb.retrieveAll().getOrElse(defXAction.className, None)
        if (cx == 0) defXAction.name
        else if (cx == 1) curXShortcutOpt match {
          case Some(shortcut) => shortcut
          case None => NoShortcutRepr
        }
        else defXAction.shortcut.getOrElse(NoShortcutRepr)
      }
    override def setValueAt(value: AnyRef, rx: Int, cx: Int) = ()
    override def addTableModelListener(l: TableModelListener) = ()
    override def removeTableModelListener(l: TableModelListener) = ()
  }

  class ShortcutCellRenderer extends DefaultTableCellRenderer {

    override def getTableCellRendererComponent(
      tab: JTable, value: AnyRef, sel: Boolean, foc: Boolean,
      rx: Int, cx: Int): ShortcutCellRenderer = {
        super.getTableCellRendererComponent(tab, value, sel, foc, rx, cx)

        val (rs, cs) = (tab.getSelectedRow(), tab.getSelectedColumn())
        val outerBorder =
          if (rs == rx)
            UIManager.getBorder("Table.focusCellHighlightBorder")
          else BorderFactory.createEmptyBorder(1, 1, 1, 1)
        val innerBorder =
          BorderFactory.createEmptyBorder(0, leftRightPad, 0, leftRightPad)
        this setBorder new CompoundBorder(outerBorder, innerBorder)

        val background =
          if (rs == rx && cx == 1) tab.getSelectionBackground()
          else tab.getBackground()
        this setBackground background
        this
    }
  }
}
