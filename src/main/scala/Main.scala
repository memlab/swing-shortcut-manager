package edu.upenn.psych.memory.shortcutmanager

object Main {

  def main(args: Array[String]) = {
    val namespace = "/" + getClass.getPackage.getName.replace(".", "/")
    val resourcePath = "/actions.xml"
    Option(Main.getClass.getResource(resourcePath)) match {
      case Some(url) => new ShortcutManager(url, namespace).setVisible(true)
      case None => Console.err println "no keyboard shortcuts file found"
    }
  }
}

