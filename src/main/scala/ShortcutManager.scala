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

abstract class XActionListener() {
  def xActionUpdated(action: XAction, oldShortcut: Option[Shortcut])
}

class ShortcutManager(url: URL, namespace: String, listener: XActionListener)
  extends JFrame {

  private val defaultXActions = new XActionParser(url).xactions
  val userdb = new UserDB(namespace, defaultXActions, listener)
  userdb persistDefaults false

  this setSize(
    new Dimension(
      800,
      ContentPane.getPreferredSize.getWidth.toInt))
  this setDefaultCloseOperation WindowConstants.DO_NOTHING_ON_CLOSE
  this addWindowListener EscapeWindowListener
  this setTitle "Keyboard Shortcuts Manager"
  this setContentPane ContentPane

  val curShortMap = userdb.retrieveAll()
  for (id <- curShortMap.keys) {
    val defaultXActionOpt = defaultXActions.find{ _.id == id }
    defaultXActionOpt match {
      case Some(xAction) => {
        val shortOpt = curShortMap(id)
        val newXAction = xAction.copy(shortcut = userdb.retrieve(id))
        listener xActionUpdated (newXAction, shortOpt)
      }
      case None =>
    }
  }

  object ContentPane extends JPanel {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
    add(Scroller)
    add(Box.createVerticalBox)
    add(ResetButtonPanel)

    object Scroller extends JScrollPane {

      this setViewportView new ShortcutTable(defaultXActions.toArray,
                                              userdb, listener)

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
            userdb persistDefaults true
            ContentPane.this.repaint()
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

class ShortcutTable(defaultXActions: Array[XAction], userdb: UserDB,
                    listener: XActionListener) extends JTable {

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
  override def getModel(): ShortcutTableModel.type = ShortcutTableModel

  object ShortcutKeyAdapter extends KeyAdapter {

    private val tab = ShortcutTable.this

    private val standaloneKeyCodes =
      Set(KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT)
    private val maskKeyCodes =
      Set(KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_ALT,
          KeyEvent.VK_META)

    override def keyPressed(e: KeyEvent) {
      val modifiers = e.getModifiers
      val code = e.getKeyCode
      val rs = tab.getSelectedRow()

      if (rs >= 0) {
        def doSwap(toSwapIn: XAction) = {
          val shortOpt: Option[Shortcut] = toSwapIn.shortcut
          val dupShortOpt: Option[Shortcut] =
            userdb.retrieveAll().values.flatten.find { short1 =>
              shortOpt match {
                case Some(short2) => short1 == short2
                case None => false
              }
            }
          dupShortOpt match {
            case Some(short: Shortcut) => {
              val msg = short + " is already taken. "
              JOptionPane.showMessageDialog(
                tab, msg, "Error", JOptionPane.OK_OPTION
              )
            }
            case None => {
              userdb.store(toSwapIn)
              tab.repaint()
            }
          }
        }

        //this is what the user entered
        val enteredShortcut: Shortcut =
          Shortcut(KeyStroke.getKeyStroke(code, modifiers))

        //what's currently in the table
        val rowXAct: XAction = tab.getModel.xactionForRow(rs)

        if((code == KeyEvent.VK_BACK_SPACE || code == KeyEvent.VK_DELETE) &&
            modifiers == 0) {
          val newXAct = rowXAct.copy(shortcut = None)
          doSwap(newXAct)
        }
        else if (maskKeyCodes contains code) {
          //this is just the press of a mask
        }
        else {
          val newXAct = rowXAct.copy(shortcut = Some(enteredShortcut))
          if (modifiers == InputEvent.SHIFT_DOWN_MASK || modifiers == 0) {
            if (standaloneKeyCodes contains code) doSwap(newXAct)
            else None
          }
          else doSwap(newXAct)
        }
      }
    }
  }

  object ShortcutTableModel extends TableModel {

    val headers = List("Action", "Shortcut", "Default")
    val NoShortcutRepr = ""

    def xactionForRow(rx: Int): XAction = defaultXActions(rx)

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
        val key = defXAction.id
        val map: Map[String, Option[Shortcut]] = userdb.retrieveAll()
        val curXShortcutOpt: Option[Shortcut] = map.getOrElse(key, None)
        if (cx == 0) defXAction.name
        else if (cx == 1) {
          curXShortcutOpt match {
            case Some(shortcut) => shortcut
            case None => NoShortcutRepr
          }
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
        if (!(rs == rx && cx == 1)) this setForeground Color.BLACK
        this
    }
  }
}
