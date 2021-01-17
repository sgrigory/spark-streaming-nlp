package com.test


import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext
import org.apache.spark.sql.functions._
import org.apache.spark.streaming._
import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline


/**
 * @author ${user.name}
 */
object Client {

	 def main(args : Array[String]) {
	 	println("HELLO")
	 	
		val pipeline = PretrainedPipeline("analyze_sentiment", "en")

		val spark = SparkSession
			 .builder()
			 .appName("Example")
			 .getOrCreate()
		val sc = spark.sparkContext
		val sqlContext= new org.apache.spark.sql.SQLContext(sc)
		import sqlContext.implicits._

		val ssc = new StreamingContext(sc, Seconds(4))


		val lines = ssc.socketTextStream("localhost", 9093)
		lines.foreachRDD(y => println(y.collect().mkString(" ")))
		val words = lines.map(_.split(" ").map(x => (x, 1)))
		val nWords = lines.map(_.size).reduce(_ + _)
		//nWords.print()
		val wordCounts = words.map(line => line.groupBy(_._1).mapValues(x => x.map(y => y._2).sum).map(identity))
		wordCounts.filter(!_.isEmpty).print()
		//wordCounts.foreachRDD(y => println(y.collect().toList))
		

		val res = lines.foreachRDD(x => pipeline.transform(x.toDF("text"))
												.select("text", "sentiment")
												.withColumn("result", col("sentiment")(0)("result"))
												.drop("sentiment")
												.show(truncate=false))
		//res.println()
		
		ssc.start()
		ssc.awaitTermination()
			
	 }




}