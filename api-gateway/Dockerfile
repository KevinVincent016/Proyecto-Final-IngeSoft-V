
FROM openjdk:11
# ARG PROJECT_VERSION=0.1.0
ARG SPRING_PROFILES_ACTIVE=dev
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}
RUN mkdir -p /home/app
WORKDIR /home/app
# ENV SPRING_PROFILES_ACTIVE dev
COPY proxy-client/target/proxy-client-v0.1.0.jar proxy-client.jars
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "api-gateway.jar"]