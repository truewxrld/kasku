#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
if [ ! -d target/dependency ]; then mvn clean package; fi
exec java -cp 'target/classes:target/dependency/*' id.kasku.Main
