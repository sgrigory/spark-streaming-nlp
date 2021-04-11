#!/bin/bash

SPARK_MASTER_PORT=7077

bin/spark-class org.apache.spark.deploy.master.Master &
bin/spark-class org.apache.spark.deploy.worker.Worker spark://spark:${SPARK_MASTER_PORT} &
bin/spark-submit --master spark://spark:${SPARK_MASTER_PORT} --driver-memory 2G --files "/usr/home/application.properties,/usr/home/reference.conf" --conf 'spark.executor.extraJavaOptions=-Dconfig.resource=reference.conf' --class com.test.Client /usr/home/streaming-0.0.1-allinone.jar
