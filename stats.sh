#!/bin/bash

java -version
echo ""

echo "building..."
rm -f recommendations.json
mvn clean verify -q

./stats-time.sh java -cp jars/genealogy.jar:jars/genealogists.jar org.codefx.java_after_eight.Main /home/nipa/code/Java-After-Eight/articles/ recommendations.json
./stats-code.sh
