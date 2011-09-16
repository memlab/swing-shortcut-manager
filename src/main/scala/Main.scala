package edu.upenn.psych.memory.shortcutmanager

object Main {

  def main(args: Array[String]) = {
    val namespace = "/" + getClass.getPackage.getName.replace(".", "/")
    val resourcePath = "/actions.xml"
    Option(Main.getClass.getResource(resourcePath)) match {
      case Some(url) => {
        val listener = new XActionListener() {
          override def xActionUpdated(xaction: XAction,
                                      old: Option[Shortcut]) =
            println("heard " + xaction + " formerly " + old)
        }
        new ShortcutManager(url, namespace, listener).setVisible(true)
      }
      case None => Console.err println "no keyboard shortcuts file found"
    }
  }
}

