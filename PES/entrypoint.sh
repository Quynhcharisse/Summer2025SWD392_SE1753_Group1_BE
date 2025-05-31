#!/bin/bash

echo "⏳ Waiting for MySQL to be ready..."
./wait-for-it.sh mysql_container:3306 --timeout=30 --strict -- echo "✅ MySQL is up"

echo "🚀 Starting Spring Boot application..."
./mvnw spring-boot:run
