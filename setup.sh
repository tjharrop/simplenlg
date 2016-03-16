sudo apt-get install -y software-properties-common
sudo apt-get install python-software-properties
sudo  add-apt-repository -y ppa:webupd8team/java 
sudo apt-get install -y libcurl3 curl

JAVA_HOME=/usr/lib/jvm/java-8-oracle
MAVEN_VERSION=3.3.9

sudo curl -fsSL --insecure https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share   mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven   && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn 
JAVA_HOME=/usr/lib/jvm/java-8-oracle
RELEASE_DATE=v20151106
sudo wget http://download.eclipse.org/jetty/${JETTY_VERSION}.${RELEASE_DATE}/dist/jetty-distribution-${JETTY_VERSION}.${RELEASE_DATE}.tar.gz &&     tar -xzvf jetty-distribution-${JETTY_VERSION}.${RELEASE_DATE}.tar.gz &&     rm -rf jetty-distribution-{{JETTY_VERSION}}.{{RELEASE_DATE}}.tar.gz &&     mv jetty-distribution-${JETTY_VERSION}.${RELEASE_DATE}/ /opt/jetty
sudo mv jetty-distribution-9.3.6.v20151106/ /opt/jetty
sudo useradd jetty &&     chown -R jetty:jetty /opt/jetty &&     rm -rf /opt/jetty/webapps.demo

sudo apt-get install maven
sudo update-java-alternatives -s java-8-oracle

sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install -y oracle-java8-installer