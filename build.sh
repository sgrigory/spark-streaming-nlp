#!/bin/sh

docker build -f spark.Dockerfile -t spark .
docker build -f app.Dockerfile -t scraper .