package com.test

import java.net.ServerSocket
import java.io.PrintStream
import java.io.DataOutputStream
import java.net.Socket
import java.net.InetAddress

import org.htmlcleaner.HtmlCleaner

/**
 * @author ${user.name}
 */
object App {

  def cleanMessage(s: String): String = {
    
    s.replaceAll("[ .,;—«»]|(&nbsp)|(&#33)", " ")
  }
  val url = "https://t.me/s/nytimes_world" //"/nexta_tv"
  
  def main(args : Array[String]) {
    println( "Hello World!" )
    val server = new ServerSocket(9093)

	val s = server.accept
	println("Connection accepted")
	val printStream = new PrintStream(s.getOutputStream())
	 while(true) { 
	    println("New iteration")
	    val resp = scala.io.Source.fromURL(url).mkString
	    val cleaner = new HtmlCleaner
	    val node = cleaner.clean(resp)
	    val msgClass = "tgme_widget_message_text js-message_text"
	    val msgs = node.getElementsByAttValue("class", msgClass, true, false)
	    val msgsCleaned = msgs.map(x=> cleanMessage(x.getText.toString).split(" ").filter(_.length > 0).mkString(" "))
	    msgsCleaned.foreach(x => println(x.take(400)))
	    msgsCleaned.foreach(printStream.println)
	    println("------------------")
	    Thread.sleep(5000)
	 }
  }

}
