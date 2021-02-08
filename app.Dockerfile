FROM openjdk:8

COPY target/streaming-0.0.1-jar-with-dependencies.jar /usr/streaming/target/
COPY application.properties run_scraper.sh /usr/streaming/
WORKDIR /usr/streaming/

CMD ["/bin/sh", "./run_scraper.sh"]