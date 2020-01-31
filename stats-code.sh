#!/bin/bash
set -e

find . -type f -name '*.java' -exec cat {} \; | sed '/^\s*#/d;/^\s*$/d;/^\s*\/\//d' | wc > /tmp/wc.$$ $@
awk '{ printf "Code stats: %s lines, %s words, %s characters\n", $1, $2, $3 }' /tmp/wc.$$
