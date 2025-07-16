FROM openjdk:17-jdk-slim

WORKDIR /app

COPY . .

RUN chmod +x ./mvnw

RUN ./mvnw clean package -DskipTests

EXPOSE 8080

#DOCKER
COPY wait-for-it.sh .
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh wait-for-it.sh

CMD ["./entrypoint.sh"]

#DEPLOY DB
#ENTRYPOINT ["java", "-jar", "target/PES-0.0.1-SNAPSHOT.jar"]
