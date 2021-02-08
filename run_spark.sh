#!/bin/bash

SPARK_MASTER_PORT=7077

bin/spark-class org.apache.spark.deploy.master.Master &
bin/spark-class org.apache.spark.deploy.worker.Worker spark://spark:${SPARK_MASTER_PORT}