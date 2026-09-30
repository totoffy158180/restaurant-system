#!/bin/bash
# 需要 JDK 17；若系統預設不是 17，請先 export JAVA_HOME=<你的 JDK 17 路徑>
cd "$(dirname "$0")"
mvn -pl sky-server -am spring-boot:run
