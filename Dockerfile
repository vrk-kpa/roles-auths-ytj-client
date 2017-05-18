FROM roles-auths-docker-virtual.vrk-artifactory-01.eden.csc.fi/roles-auths-java-base@sha256:d3627ed406b05342b36ab74db0c8f59eef8a5eefda7fac48278896aa7cdecc68

# Deploy project
RUN mkdir -p /opt/rova/roles-auths-ytj-client/
ADD target/roles-auths-ytj-client.jar /opt/rova/roles-auths-ytj-client/
ADD service.properties.template /opt/rova/roles-auths-ytj-client/
ADD LICENSE /opt/rova/roles-auths-ytj-client/license/LICENSE
ADD target/site /opt/rova/roles-auths-ytj-client/license/dependency-report
WORKDIR /opt/rova/roles-auths-ytj-client/

EXPOSE 8080
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Xms256m", "-Xmx512m", "-jar", "roles-auths-ytj-client.jar"]
