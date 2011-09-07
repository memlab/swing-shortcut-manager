package edu.upenn.psych.memory.keyboardmanager

object Main {

  def main(args: Array[String]) = {
    val resourcePath = "/actions.xml"
    Option(Main.getClass.getResourceAsStream(resourcePath)) match {
      case Some(stream) => {
        val xactions = new XActionsParser(stream).xactions
        val mgr = new ShortcutManager(xactions)
        mgr setVisible true
      }
      case None => Console.err println "no keyboard shortcuts file found"
    }
  }
}

