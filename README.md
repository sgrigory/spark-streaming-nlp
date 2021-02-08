# Sentiment analysis on Spark Structured Streaming

This app illustrates usage of Spark Structured Streaming and Spark NLP. It scrapes a web page, feeds the results into a Spark stream, and applies sentiment analysis to count the number of positive, negative, and neutral texts.
The app consists of two parts:
- `src/main/scala/com/client.scala` scrapes the page and sends the cleaned results to the client through a TCP socket
- `src/main/scala/com/App.scala` receives the cleaned results into a Structured Stream, applies sentiment analysis and aggregates the results

Configurations are in `application.properties`

# Build & run

```
./build.sh

docker-compose up

spark-submit --deploy-mode cluster --master spark://localhost:7077 --driver-memory 2G --files /usr/home/application.properties --class com.test.Client /usr/home/streaming-0.0.1-jar-with-dependencies.jar
```

