#!/bin/bash

java -version
echo ""

echo "building..."
mvn clean verify -q

./stats-time.sh java -jar target/recommend.jar /home/nipa/code/Java-After-Eight/articles/ recommendations.json
./stats-code.sh
