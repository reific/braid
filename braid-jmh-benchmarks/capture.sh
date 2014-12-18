#!/usr/bin/env bash
git diff --quiet          || { echo "Working directory is not clean. Exiting."                  ; exit 1; }
git diff --cached --quiet || { echo "Working directory is not clean (staged changes). Exiting." ; exit 1; }

mkdir -p results
output_file="results/$(git rev-parse HEAD).txt"
echo $output_file
lscpu > $output_file
mvn clean package && java -jar target/benchmarks.jar -f 5 >> $output_file
