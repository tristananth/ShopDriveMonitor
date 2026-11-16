#!/bin/bash
cd "$(dirname "$0")"
[ -d out ] || ./build.sh
java -cp out shopdrive.Main "$@"          # tambahkan --console atau --tanggal=2026-11-16 bila perlu
