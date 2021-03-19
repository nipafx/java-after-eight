#!/bin/bash
set -e

echo "building..."
rm -f recommendations.json
rm -rf jars/*
mvn clean verify -q
