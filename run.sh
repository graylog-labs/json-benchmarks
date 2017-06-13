#!/bin/sh
set -e
mvn package
# java -jar target/benchmarks.jar -wi 3 -i 3 -f 1 -t 8
java -jar target/benchmarks.jar $@
