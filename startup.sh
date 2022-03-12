#!/bin/bash
# Startup script

# read -p "Enter JAVA_HOME [/usr/lib/jvm/java-17-oracle]: " java_input
java=${java_input:-/usr/lib/jvm/java-17-oracle}
echo $java

export JAVA_HOME=$java
export PATH=$JAVA_HOME/bin:$PATH

printf "\n"
echo "Install mongodb service"
./mvnw -f pom.xml clean install -U

printf "\n"
echo "Creating environment with docker..."
echo "Using development configuration ./config/env.dev"
docker-compose up --build --force-recreate -d