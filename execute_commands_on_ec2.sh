#!/usr/bin/env bash

kill -9 $(sudo lsof -t -i:8082)
echo "Killed process running on port 8082"

echo "Starting server "
java -jar charges-processing-0.0.1-SNAPSHOT.jar