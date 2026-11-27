#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mvn -q compile
aspectj_jar="${ASPECTJ_JAR:-$HOME/.m2/repository/org/aspectj/aspectjweaver/1.9.25.1/aspectjweaver-1.9.25.1.jar}"
if [[ ! -f "$aspectj_jar" ]]; then
    echo "Indiquer le chemin du JAR AspectJ avec ASPECTJ_JAR." >&2
    exit 1
fi
exec java "-javaagent:$aspectj_jar" -cp "target/classes:$aspectj_jar" org.example.Main
