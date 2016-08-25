FROM openjdk:8

RUN apt-get update && apt-get install -y openjfx

COPY maven/documents /tmp/documents
COPY maven/index /tmp/index
COPY maven/JsonDocuments /tmp/JsonDocuments
COPY maven/target/Lucene-Prototype-1.0.war /tmp/
COPY maven/target/dependency/jetty-runner.jar /tmp/

# Entrypoint provided in Project pom
#ENTRYPOINT ["java","-jar","/tmp/Lucene-Prototype-1.0-jar-with-dependencies.jar"]
