FROM openjdk:17-jdk-slim

WORKDIR /app

COPY . .

RUN chmod +x ./mvnw

RUN ./mvnw clean package -DskipTests

RUN ./mvnw dependency:go-offline

COPY wait-for-it.sh .
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh wait-for-it.sh

EXPOSE 8080

CMD ["./entrypoint.sh"]