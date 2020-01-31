#!/bin/bash

java -version
echo ""

echo "building..."
rm -f recommendations.json
mvn clean verify -q

./stats-time.sh java -cp jars/genealogy.jar:jars/genealogists.jar org.codefx.java_after_eight.Main
./stats-code.sh
