#!/bin/bash

java -cp target/streaming-0.0.1-allinone.jar com.test.App #> scraper_logs.txt &

#spark-submit --driver-memory 16G --class com.test.Client target/streaming-0.0.1-jar-with-dependencies.jar


