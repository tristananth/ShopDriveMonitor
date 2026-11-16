#!/bin/bash
# Kompilasi: butuh JDK 11+ (javac)
cd "$(dirname "$0")"
rm -rf out && mkdir out
javac -encoding UTF-8 -d out $(find src -name "*.java") && echo "Build berhasil. Jalankan ./run.sh"
