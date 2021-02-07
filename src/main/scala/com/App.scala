package com.test

import java.net.ServerSocket
import java.io.PrintStream
import java.io.DataOutputStream
import java.net.Socket
import java.net.InetAddress

import scala.io.Source

import org.htmlcleaner.HtmlCleaner


/**
 * @author ${user.name}
 */
object App {

  
  def cleanMessage(s: String): String = {
    // Get rid of special symbols and 
    s.replaceAll("[ .,;—«»]|(&nbsp)|(&#33)", " ")
  }
  

  def main(args : Array[String]) {

  	println("Starting scraper.." )

  	// Load configuration from the properties file
  	loadProperties("application.properties")
  	
  	// Open the port and wait for a connection
  	val server = new ServerSocket(port)
  	val s = server.accept
	println("Connection accepted")
	val printStream = new PrintStream(s.getOutputStream())

	// Prepare the HTML parser
	val cleaner = new HtmlCleaner
	val msgClass = "tgme_widget_message_text js-message_text"

	// Scrape the page with a given interval
	while(true) { 
	    println("New iteration")
	    
	    // Get the page HTML
	    val resp = scala.io.Source.fromURL(url).mkString
	    
	    // Parse the HTML and extract the text elements
	    val node = cleaner.clean(resp)
	    val msgs = node.getElementsByAttValue("class", msgClass, true, false)

	    // For every text element, extract and clean the content
	    val msgsCleaned = msgs.map(x=> {
                                    val chldr = x.getChildTags
                                    val txt = chldr(0).getText.toString
                                    cleanMessage(txt).split(" ").filter(_.length > 0).mkString(" ")
                                    }
                              )
	    
	    // Print the cleaned text to console
	    msgsCleaned.foreach(x => println(x.take(maxMessageSize)))
	    
	    // Send the cleaned text to the client
	    msgsCleaned.foreach(printStream.println)
	    println("------------------")

	    // Wait before the next iteration
	    Thread.sleep(interval)
	 }
  }

}
