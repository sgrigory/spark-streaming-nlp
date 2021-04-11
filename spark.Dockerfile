FROM openjdk:8

ARG spark_version=2.4.7
ARG hadoop_version=2.7
ARG spark_master_web_ui=8080


RUN  curl https://archive.apache.org/dist/spark/spark-${spark_version}/spark-${spark_version}-bin-hadoop${hadoop_version}.tgz -o spark.tgz && \
    tar -xf spark.tgz && \
    mv spark-${spark_version}-bin-hadoop${hadoop_version} /usr/bin/

ENV SPARK_HOME /usr/bin/spark-${spark_version}-bin-hadoop${hadoop_version}

ENV SPARK_MASTER_PORT 7077

EXPOSE ${SPARK_MASTER_PORT} ${spark_master_web_ui}

COPY run_spark.sh ${SPARK_HOME}/
COPY application.properties ${SPARK_HOME}/
COPY application.properties src/main/resources/reference.conf /usr/home/

COPY target/streaming-0.0.1-allinone.jar /usr/home/

WORKDIR ${SPARK_HOME}

CMD ./run_spark.sh