FROM docker-registry.kapa.ware.fi/roles-auths-java-base@sha256:84db8eb95099600b8fe3b0d072145da6a679cd2646b559ac3d2ddbea2717805a

# Deploy project
RUN mkdir -p /opt/rova/roles-auths-ytj-client/
ADD target/roles-auths-ytj-client.jar /opt/rova/roles-auths-ytj-client/
ADD service.properties.template /opt/rova/roles-auths-ytj-client/
ADD LICENSE /opt/rova/roles-auths-ytj-client/license/LICENSE
ADD target/site /opt/rova/roles-auths-ytj-client/license/dependency-report
WORKDIR /opt/rova/roles-auths-ytj-client/

EXPOSE 8080
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Xms256m", "-Xmx512m", "-jar", "roles-auths-ytj-client.jar"]
