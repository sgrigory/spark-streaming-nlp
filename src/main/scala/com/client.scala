package com.test

import java.sql.Timestamp
import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext
import org.apache.spark.sql.functions._

import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline


/**
 * @author ${user.name}
 */
object Client {


	 def main(args : Array[String]) {

	 	println("Staring sentiment analyser")

	 	// Load configuration from the properties file
	 	loadProperties("application.properties")
	 	
	 	// Load the Spark NLP pipeline
	 	val pipeline = PretrainedPipeline("analyze_sentimentdl_use_imdb", "en")

	 	// Get Spark session created by Spark NLP
		val spark = SparkSession
			 .builder()
			 .appName("Example")
			 //.config("spark.executor.memory", "16g")
			 .getOrCreate()

		// Get rid of excessive logging
		val sc = spark.sparkContext
		sc.setLogLevel("ERROR")

		// Import serializers for basic types
		val sqlContext= new org.apache.spark.sql.SQLContext(sc)
		import sqlContext.implicits._

		//  Read stream of text messages from a socket
		val lines = spark.readStream.format("socket")
									.option("host", "localhost")
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
		val queryCounts = counts.writeStream.outputMode("complete").format("console").start()

		spark.streams.awaitAnyTermination()
			
	 }




}