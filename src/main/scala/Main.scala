package edu.upenn.psych.memory.keyboardmanager

object Main {

  def main(args: Array[String]) = {
    val resourcePath = "/actions.xml"
    Option(Main.getClass.getResourceAsStream(resourcePath)) match {
      case Some(stream) => {
        val xactions = new XActionsFile(stream).xactions
        val mgr = new KeyboardShortcutManager(xactions)
        mgr setVisible true
      }
      case None => Console.err println "no keyboard shortcuts file found"
    }
  }
}

