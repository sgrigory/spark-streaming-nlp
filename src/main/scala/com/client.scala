package com.test

import java.sql.Timestamp
import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext
import org.apache.spark.sql.functions._
import org.apache.spark.streaming._
import scala.collection.mutable.Queue

import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline


/**
 * @author ${user.name}
 */
object Client {


	 def main(args : Array[String]) {
	 	println("HELLO")
	 	// analyze_sentiment
	 	// analyze_sentimentdl_use_imdb
		val pipeline = PretrainedPipeline("analyze_sentiment", "en")

		val spark = SparkSession
			 .builder()
			 .appName("Example")
			 //.config("spark.executor.memory", "16g")
			 .getOrCreate()
		val sc = spark.sparkContext
		sc.setLogLevel("ERROR")
		val sqlContext= new org.apache.spark.sql.SQLContext(sc)
		import sqlContext.implicits._

		// val ssc = new StreamingContext(sc, Seconds(4))
		// val lines = ssc.socketTextStream("localhost", 9093)
		// lines.foreachRDD(y => println(y.collect().mkString(" ")))
		// val words = lines.map(_.split(" ").map(x => (x, 1)))
		// val nWords = lines.map(_.size).reduce(_ + _)
		// //nWords.print()
		// val wordCounts = words.map(line => line.groupBy(_._1).mapValues(x => x.map(y => y._2).sum).map(identity))
		// wordCounts.filter(!_.isEmpty).print()
		// //wordCounts.foreachRDD(y => println(y.collect().toList))
		

		// val res = lines.foreachRDD(x => pipeline.transform(x.toDF("text"))
		// 										.select("text", "sentiment")
		// 										.withColumn("result", col("sentiment")(0)("result"))
		// 										.drop("sentiment")
		// 										.show(truncate=false))
		//res.println()
		// ssc.start()
		// ssc.awaitTermination()

		val lines = spark.readStream.format("socket")
									.option("host", "localhost")
									.option("port", 9093)
									.option("includeTimestamp", true).load()
		val linesUnique = lines.as[(String, Timestamp)].withColumnRenamed("value", "text")
													 .withWatermark("timestamp", "50 seconds")
													 .dropDuplicates("text")
		val sentim = pipeline.transform(linesUnique)
							 .withColumn("result", col("sentiment")(0)("result"))
							 .select("text", "result")
		val counts = sentim.groupBy("result").count()

		//val union = linesUnique.withColumn("result", lit("")).withColumn("count", lit(0)).union(counts.withColumn("text", lit("")))

		// val joined = counts.withWatermark("timestamp", "10 minutes")
		// 			.join(sentim.withWatermark("timestamp", "10 minutes"), usingColumn="result")

		// val queryJoined = joined.writeStream.outputMode("append").format("console").start()
		//val queryText = sentim.writeStream.outputMode("update").format("console").start()
		val queryCounts = counts.writeStream.outputMode("complete").format("console").start()

		//val queryUnion = union.writeStream.outputMode("complete").format("console").option("truncate", "false").start()
		
		spark.streams.awaitAnyTermination()
			
	 }




}