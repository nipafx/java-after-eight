#!/bin/bash
set -e

java -version
echo ""

./build.sh
./stats-time.sh ./run.sh
./stats-code.sh
