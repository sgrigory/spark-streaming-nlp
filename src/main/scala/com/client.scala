package com.test

import scala.io.Source
import scala.concurrent.Future
import java.sql.Timestamp
import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext
import org.apache.spark.sql.functions._
import org.apache.spark.SparkFiles

import akka.actor.ActorSystem

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._


import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.Route


import com.typesafe.config.ConfigFactory

import scala.util.Random


import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline


/**
 * @author ${user.name}
 */
object Client {


	 def main(args : Array[String]) {

	 	println("Staring sentiment analyser")

	 	// Get Spark session created by Spark NLP
		val spark = SparkSession
			 .builder()
			 .appName("SparkStreamingNLP")
			 .master("spark://spark:7077")
			 //.config("spark.executor.memory", "16g")
			 .getOrCreate()

		// Load configuration from the properties file
	 	val propertiesFile = SparkFiles.get("application.properties")
	 	loadProperties(propertiesFile)
	 	
	 	// Load the Spark NLP pipeline
	 	println("Using pipeline " + pipelineName)
	 	val pipeline = PretrainedPipeline(pipelineName, "en")

		// Get rid of excessive logging
		val sc = spark.sparkContext
		sc.setLogLevel("ERROR")

		// Import serializers for basic types
		val sqlContext= new org.apache.spark.sql.SQLContext(sc)
		import sqlContext.implicits._

		//  Read stream of text messages from a socket
		val lines = spark.readStream.format("socket")
									.option("host", scraperHost)
									.option("port", port)
									.option("includeTimestamp", true).load()

		// Deduplicate the stream - making sure same message wasn't recorded twice
		val linesUnique = lines.as[(String, Timestamp)].withColumnRenamed("value", "text")
													 .withWatermark("timestamp", watermark)
													 .dropDuplicates("text")
		// Apply sentiment analysis on every message
		val sentim = pipeline.transform(linesUnique)
							 .withColumn("result", col("sentiment")(0)("result"))
							 .select("text", "result")
		
		// Count the number of positive, negative, neutral, and NA outcomes
		val counts = sentim.groupBy("result").count()

		// Output the count to the console
		//val queryCounts = counts.writeStream.outputMode("complete").format("console").start()
		
		// Output the count to a table in memory
		val queryCounts = counts.writeStream.outputMode("complete").queryName("results").format("memory").start()

		implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

		// Process the Spark stream defined above in a non-blocking way
		Future {
			println("----- awaitAnyTermination ------")
			spark.streams.awaitAnyTermination()
		}

		// -----------------------------------------------
	 	// Start the REST API server to output the results
	 	// -----------------------------------------------

	 	val smallConfig = ConfigFactory.parseString("""
			akka.log-config-on-start = on
			akka.actor.enable-additional-serialization-bindings = on
			""")

	 	println("creating ActorSystem .......")
	 	implicit val system = ActorSystem("Server", smallConfig)

		// Server definition
		object WebServer extends HttpApp {
		  override def routes: Route =
		    path("hello") {
		      get {
		        complete(spark.sql("select * from results").toJSON.collect().mkString("[", ", ", "]"))
		    
		      }
		    }
		}

		// Starting the server in a non-blocking way
		Future {
			WebServer.startServer("0.0.0.0", restPort)
		}

			
	 }




}