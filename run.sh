#!/usr/bin/env bash
set -euo pipefail
mkdir -p out
javac -d out src/textanalysis/*.java
java -cp out textanalysis.Tests
java -cp out textanalysis.Benchmark
python3 plot_results.py
