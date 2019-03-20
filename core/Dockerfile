FROM openjdk:8-jre-alpine
MAINTAINER Jeroen van Maanen <jeroen@leialearns.org>

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/service/service.jar"]

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
##ADD target/lib           /usr/share/service/lib
# Add the service itself
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/service/service.jar
