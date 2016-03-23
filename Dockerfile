FROM java:8
MAINTAINER Jouella Fabe <jojofabe@gmail.com> 
#installs java


LABEL "description"="backend of simplenlg frontend angularJS application" "version"="0.0.1"



RUN mkdir /home/simplenlg /home/simplenlg/lib /home/simplenlg/repo /home/simplenlg/src /home/simplenlg/target
RUN apt-get update && apt-get install -y vim 
ENV MAVEN_HOME /usr/share/maven
VOLUME /root/.m2
ADD Procfile /home/simplenlg
ADD lib /home/simplenlg/lib
ADD pom.xml /home/simplenlg
ADD repo /home/simplenlg/repo
ADD src /home/simplenlg/src
ADD target /home/simplenlg/target

WORKDIR /home/simplenlg

EXPOSE 4567 
CMD ["java", "-jar", "target/simplenlg-0.0.1-SNAPSHOT-jar-with-dependencies.jar"]

