#!/bin/bash

echo "running..."

rm -f /tmp/mtime.$$
for x in {1..10}
do
  /usr/bin/time -f "real %e user %U sys %S" -a -o /tmp/mtime.$$ $@
#  tail -1 /tmp/mtime.$$
done

echo ""
awk '{ et += $2; ut += $4; st += $6; count++ } END { printf "Average runtime (user time): %.3fs\n", ut/count }' /tmp/mtime.$$
