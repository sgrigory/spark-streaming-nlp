package com.test

import java.net.ServerSocket
import java.io.PrintStream
import java.io.DataOutputStream
import java.net.Socket
import java.net.InetAddress

import java.util.Properties
import scala.io.Source
import java.io.FileInputStream

import org.htmlcleaner.HtmlCleaner

/**
 * @author ${user.name}
 */
object App {

  
  var url = ""
  var port = 0
  var interval = 0
  var maxMessageSize = 0


  def cleanMessage(s: String): String = {
    
    s.replaceAll("[ .,;—«»]|(&nbsp)|(&#33)", " ")
  }

  def loadProperties(configFile: String): Unit = {

  	val properties: Properties = new Properties()
	val inStream = new FileInputStream(configFile)
	properties.load(inStream)
	inStream.close()
	port = properties.getProperty("port").toInt
	interval = properties.getProperty("interval").toInt
	maxMessageSize = properties.getProperty("message_max_size").toInt
	url = properties.getProperty("url")

  }

  
  def main(args : Array[String]) {

  	println("Loading configuration.." )
  	loadProperties("application.properties")
  	
  	val server = new ServerSocket(port)

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
	     val msgsCleaned = msgs.map(x=> {
                                    val chldr = x.getChildTags
                                    val txt = chldr(0).getText.toString
                                    cleanMessage(txt).split(" ").filter(_.length > 0).mkString(" ")
                                    }
                              )
	    msgsCleaned.foreach(x => println(x.take(maxMessageSize)))
	    msgsCleaned.foreach(printStream.println)
	    println("------------------")
	    Thread.sleep(interval)
	 }
  }

}
