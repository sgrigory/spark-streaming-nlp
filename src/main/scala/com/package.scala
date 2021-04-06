package com

import java.io.FileInputStream
import java.util.Properties


package object test {

	var url = ""
    var port = 0
    var interval = 0
    var maxMessageSize = 0
    var watermark = ""
    var pipelineName = ""
    var restPort = 0
    var scraperHost = ""


	def loadProperties(configFile: String): Unit = {
		// Load configuration from a properties file

	  	val properties: Properties = new Properties()
		val inStream = new FileInputStream(configFile)
		properties.load(inStream)
		inStream.close()
		// Port through with client and sever parts of the app comminucate
		port = properties.getProperty("port").toInt
		// Interval at which the page is scraped
		interval = properties.getProperty("interval").toInt
		// Limit for the message size
		maxMessageSize = properties.getProperty("message_max_size").toInt
		// URL to scrape
		url = properties.getProperty("url")
		// Time window within which the client deduplicates messages 
		watermark = properties.getProperty("watermark")
		// Spark NLP pretrained pipeline 
		pipelineName = properties.getProperty("pipeline")
		// Port for the REST API with output stats
		restPort = properties.getProperty("rest_port").toInt

		scraperHost = properties.getProperty("scraper_host")

	  }

}